package pl.ciruk.moque.jms;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.jms.server.JMSServerManager;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import java.util.HashSet;
import java.util.List;

public class MockDestination implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        Configuration config = new ConfigurationImpl();
        HashSet<TransportConfiguration> transports = new HashSet<>();

        transports.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));

        config.setAcceptorConfigurations(transports);

        EmbeddedJMS jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(config);
        jmsServer.setJmsConfiguration(new JMSConfigurationImpl());
        jmsServer.start();

        ConnectionFactory connectionFactory = (ConnectionFactory) jmsServer.lookup("ConnectionFactory");
        Destination destination = (Destination) jmsServer.lookup("/example/queue");

        context.getStore(ExtensionContext.Namespace.create("moque")).put("jmsServer", jmsServer);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        EmbeddedJMS jmsServer = context.getStore(ExtensionContext.Namespace.create("moque")).get("jmsServer", EmbeddedJMS.class);
        jmsServer.stop();
    }
}
