package pl.ciruk.moque.jms;

import org.junit.jupiter.api.extension.*;
import pl.ciruk.moque.ConnectionSupplier;
import pl.ciruk.moque.Gateway;
import pl.ciruk.moque.GatewayConsumer;
import pl.ciruk.moque.WhenReceived;
import pl.ciruk.moque.function.ThrowingPredicate;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JmsMoque implements BeforeEachCallback, AfterEachCallback {
    private final ConnectionSupplier<Connection> connectionSupplier;
    private Connection connection;
    private Gateway<TextMessage> jmsGateway;
    private Map<String, GatewayConsumer<TextMessage>> gatewayConsumerByQueueName = new HashMap<>();

    private JmsMoque() {
        throw new AssertionError("Extension must be registered manually using @RegisterExtension " +
                "and instantiated with withEmbeddedServer() or withConnectionSupplier()");
    }

    private JmsMoque(ConnectionSupplier<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    public static JmsMoque withEmbeddedServer() {
        return withConnectionSupplier(new EmbeddedConnectionSupplier());
    }

    public static JmsMoque withConnectionSupplier(ConnectionSupplier<Connection> connectionSupplier) {
        return new JmsMoque(connectionSupplier);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        connectionSupplier.beforeAll(context);

        try {
            connection = connectionSupplier.get();
            connection.start();
        } catch (Exception e) {
            throw new AssertionError(e);
        }

        jmsGateway = new JmsGateway(createSession());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        try {
            jmsGateway.close();
            closeGatewayConsumers();
            gatewayConsumerByQueueName.clear();
            connection.close();
        } catch (JMSException e) {
            throw new AssertionError(e);
        } finally {
            connectionSupplier.afterAll(context);
        }
    }

    private void closeGatewayConsumers() throws Exception {
        for (var gatewayConsumer : gatewayConsumerByQueueName.values()) {
            gatewayConsumer.close();
        }
    }

    public WhenReceived<TextMessage> whenReceived(String queueName) {
        return whenReceived(queueName, __ -> true);
    }

    public WhenReceived<TextMessage> whenReceived(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        var session = createSession();
        GatewayConsumer<TextMessage> gatewayConsumer = gatewayConsumerByQueueName.computeIfAbsent(
                queueName,
                __ -> createConsumer(queueName, session));

        WhenReceived<TextMessage> whenReceived = new WhenReceived<>(new JmsGateway(session), messageMatcher);
        gatewayConsumer.addWhenReceivedRule(whenReceived);

        return whenReceived;
    }

    public void send(String queueName, String message) {
        jmsGateway.send(queueName, message);
    }

    public TextMessage receive(String queueName) {
        return jmsGateway.receive(queueName);
    }

    public TextMessage receiveWithTimeout(String queueName, long time, TimeUnit timeUnit) {
        return jmsGateway.receiveWithTimeout(queueName, time, timeUnit);
    }

    private GatewayConsumer<TextMessage> createConsumer(String queueName, Session session) {
        try {
            MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));
            GatewayConsumer<TextMessage> gatewayConsumer = new GatewayConsumer<>(consumer::close);
            consumer.setMessageListener(message -> gatewayConsumer.onMessage((TextMessage) message));
            return gatewayConsumer;
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
}
