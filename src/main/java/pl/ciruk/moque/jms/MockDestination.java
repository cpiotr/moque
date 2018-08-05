package pl.ciruk.moque.jms;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.*;
import pl.ciruk.moque.Gateway;
import pl.ciruk.moque.ThrowingPredicate;
import pl.ciruk.moque.WhenReceived;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;

public class MockDestination implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    private Gateway<TextMessage> jmsGateway;
    private Connection connection;
    private Map<String, GatewayConsumer<TextMessage>> listeners = new HashMap<>();

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
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

        EmbeddedJMS jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(configuration);
        jmsServer.setJmsConfiguration(jmsConfig);
        jmsServer.start();

        ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("/cf");
        connection = connectionFactory.createConnection();
        connection.start();

        findStore(context).put("jmsServer", jmsServer);
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

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        connection.close();
        EmbeddedJMS jmsServer = findStore(context).get("jmsServer", EmbeddedJMS.class);
        jmsServer.stop();
    }

    public WhenReceived<TextMessage> whenReceived(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        GatewayConsumer<TextMessage> gatewayConsumer = listeners.computeIfAbsent(queueName, __ -> createConsumer(queueName, messageMatcher));

        return gatewayConsumer.getWhenReceived();
    }

    public void send(String queueName, String message) {
        jmsGateway.send(queueName, message);
    }

    @NotNull
    private GatewayConsumer<TextMessage> createConsumer(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        var session = createSession();
        JmsGateway jmsGateway = new JmsGateway(session);
        WhenReceived<TextMessage> whenReceived = new WhenReceived<>(jmsGateway, messageMatcher);
        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
            consumer.setMessageListener(message -> whenReceived.onMessage((TextMessage) message));
            return new GatewayConsumer<>(consumer, whenReceived);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }

    private Session createSession() {
        try {
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var session = createSession();

        jmsGateway = new JmsGateway(session);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        jmsGateway.close();
        for (var gatewayConsumer : listeners.values()) {
            gatewayConsumer.close();
        }
        listeners.clear();
    }

    public TextMessage receiveFrom(String queueName) {
        return jmsGateway.receive(queueName);
    }
}
