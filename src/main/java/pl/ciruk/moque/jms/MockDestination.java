package pl.ciruk.moque.jms;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import pl.ciruk.moque.Gateway;
import pl.ciruk.moque.ThrowingPredicate;
import pl.ciruk.moque.WhenReceived;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

public class MockDestination implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private Gateway<TextMessage> jmsGateway;
    private Connection connection;
    private Session session;

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        configuration.addConnectorConfiguration(
                NettyConnectorFactory.class.getName(),
                new TransportConfiguration(NettyConnectorFactory.class.getName()));

        JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        ConnectionFactoryConfiguration cfConfig = createConnectionFactoryConfiguration();
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        String queue1 = "Q1";
        JMSQueueConfiguration queueConfig = createQueueConfiguration(queue1);
        jmsConfig.getQueueConfigurations().add(queueConfig);

        EmbeddedJMS jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);
        jmsServer.start();

        ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("/cf");
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue q1 = session.createQueue(queue1);


        MessageConsumer consumer = session.createConsumer(session.createQueue("Q314"));
        consumer.setMessageListener(message -> System.out.println("Listener: " + message));
        session.setMessageListener(message -> System.out.println("Session: " + message));

        System.out.println(1);
        TextMessage textMessage = session.createTextMessage("fdfs");
        MessageProducer producer = session.createProducer(q1);
        producer.send(textMessage);
        producer.close();

        jmsGateway = new Gateway<>() {
            @Override
            public void receive(String destination, TextMessage message) {
                try {
                    MessageConsumer messageConsumer = session.createConsumer(session.createQueue(destination));
                    messageConsumer.receive();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void send(String destination, String message) {
                try (MessageProducer producer = session.createProducer(session.createQueue(destination))) {
                    producer.send(session.createTextMessage(message));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        };

        findStore(context).put("jmsServer", jmsServer);
        findStore(context).put("session", session);
    }

    private ExtensionContext.Store findStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(MockDestination.class));
    }

    @NotNull
    private ConnectionFactoryConfiguration createConnectionFactoryConfiguration() {
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl();
        cfConfig.setName("cf");
        cfConfig.setBindings("/cf");
        cfConfig.setConnectorNames(NettyConnectorFactory.class.getName());
        return cfConfig;
    }

    @NotNull
    private JMSQueueConfiguration createQueueConfiguration(String name) {
        JMSQueueConfiguration queueConfig = new JMSQueueConfigurationImpl();
        queueConfig.setName(name);
        queueConfig.setName(name);
        return queueConfig;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        session.close();
        connection.close();
        EmbeddedJMS jmsServer = findStore(context).get("jmsServer", EmbeddedJMS.class);
        jmsServer.stop();
    }

    public WhenReceived<TextMessage> whenReceived(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        return listeners.computeIfAbsent(queueName, __ -> createConsumer(queueName, messageMatcher));
    }

    @NotNull
    private WhenReceived<TextMessage> createConsumer(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        WhenReceived<TextMessage> whenReceived = new WhenReceived<>(jmsGateway, messageMatcher);
        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
            consumer.setMessageListener(message -> whenReceived.onMessage((TextMessage) message));
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
        return whenReceived;
    }

    private Map<String, WhenReceived<TextMessage>> listeners = new HashMap<>();

}
