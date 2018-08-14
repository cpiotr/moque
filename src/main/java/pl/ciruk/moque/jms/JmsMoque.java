package pl.ciruk.moque.jms;

import org.junit.jupiter.api.extension.*;
import pl.ciruk.moque.*;
import pl.ciruk.moque.function.ThrowingPredicate;

import javax.jms.*;
import java.lang.IllegalStateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JmsMoque implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {
    private final ConnectionSupplier<Connection> connectionSupplier;
    private Connection connection;
    private Gateway<TextMessage> jmsGateway;
    private Map<String, GatewayConsumer<TextMessage>> listeners = new HashMap<>();

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
    public void beforeAll(ExtensionContext context) {
        connectionSupplier.beforeAll(context);

        connection = connectionSupplier.get();
    }


    @Override
    public void afterAll(ExtensionContext context) {
        try {
            connection.close();
        } catch (JMSException e) {
            throw new AssertionError(e);
        } finally {
            connectionSupplier.afterAll(context);
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

    public WhenReceived<TextMessage> whenReceived(String queueName) {
        return whenReceived(queueName, __ -> true);
    }

    public WhenReceived<TextMessage> whenReceived(String queueName, ThrowingPredicate<TextMessage> messageMatcher) {
        var session = createSession();
        GatewayConsumer<TextMessage> gatewayConsumer = listeners.computeIfAbsent(queueName, __ -> createConsumer(queueName, session));

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
