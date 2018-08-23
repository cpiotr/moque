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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class EmbeddedServer implements AutoCloseable {
    private static final String CONNECTION_FACTORY_BINDING = "/cf";
    private final EmbeddedJMS jms;

    public EmbeddedServer() {
        jms = new EmbeddedJMS();
        jms.setConfiguration(createConfiguration());
        jms.setJmsConfiguration(createJmsConfiguration());
    }

    public void start() {
        try {
            jms.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        try {
            jms.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Connection createConnection() {
        ConnectionFactory connectionFactory = (ConnectionFactory) jms.lookup(CONNECTION_FACTORY_BINDING);
        try {
            return connectionFactory.createConnection();
        } catch (JMSException e) {
            throw new AssertionError(e);
        }
    }

    @NotNull
    private static JMSConfiguration createJmsConfiguration() {
        JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        ConnectionFactoryConfiguration cfConfig = createConnectionFactoryConfiguration();
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);
        return jmsConfig;
    }

    @NotNull
    private static Configuration createConfiguration() {
        Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        configuration.addConnectorConfiguration(
                NettyConnectorFactory.class.getName(),
                new TransportConfiguration(NettyConnectorFactory.class.getName()));
        return configuration;
    }

    @NotNull
    private static ConnectionFactoryConfiguration createConnectionFactoryConfiguration() {
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl();
        cfConfig.setName("cf");
        cfConfig.setBindings(CONNECTION_FACTORY_BINDING);
        cfConfig.setConnectorNames(NettyConnectorFactory.class.getName());
        return cfConfig;
    }
}
