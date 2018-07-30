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

import javax.jms.*;
import java.util.function.Predicate;

public class MockDestination implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
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

        String queue1 = "queue1";
        JMSQueueConfiguration queueConfig = createQueueConfiguration(queue1);
        jmsConfig.getQueueConfigurations().add(queueConfig);

        EmbeddedJMS jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);
        jmsServer.start();

        ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("/cf");
        try (var connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue q1 = session.createQueue(queue1);
            MessageConsumer consumer = session.createConsumer(q1);
            consumer.setMessageListener(message -> System.out.println("Listener: " + message));
            session.setMessageListener(message -> System.out.println("Session: " + message));

            session.createProducer(q1).send(session.createTextMessage("fdfs"));
            System.out.println(1);
            session.close();
        }

        context.getStore(ExtensionContext.Namespace.create("moque")).put("jmsServer", jmsServer);
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
        EmbeddedJMS jmsServer = context.getStore(ExtensionContext.Namespace.create("moque")).get("jmsServer", EmbeddedJMS.class);
        jmsServer.stop();
    }

    public <T> void whenReceived(String queueName, Predicate<T> messageMatcher) {

    }
}
