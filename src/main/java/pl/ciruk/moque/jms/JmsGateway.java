package pl.ciruk.moque.jms;

import pl.ciruk.moque.Gateway;
import pl.ciruk.moque.function.ThrowingSupplier;

import javax.jms.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class JmsGateway implements Gateway<TextMessage> {
    private final Session session;

    JmsGateway(Session session) {
        this.session = session;
    }

    @Override
    public TextMessage receive(String destination) {
        try {
            MessageConsumer messageConsumer = session.createConsumer(session.createQueue(destination));
            return (TextMessage) messageConsumer.receive();
        } catch (JMSException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public TextMessage receiveWithTimeout(String destination, long time, TimeUnit timeUnit) {
        try {
            MessageConsumer messageConsumer = session.createConsumer(session.createQueue(destination));
            return (TextMessage) messageConsumer.receive(timeUnit.toMillis(time));
        } catch (JMSException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void send(String destination, String message) {
        send(destination, () -> session.createTextMessage(message));
    }

    @Override
    public void send(String destination, byte[] message) {
        ThrowingSupplier<Message> messageSupplier = () -> {
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(message);
            return bytesMessage;
        };
        send(destination, messageSupplier);
    }

    private void send(String destination, ThrowingSupplier<Message> messageSupplier) {
        try (MessageProducer producer = session.createProducer(session.createQueue(destination))) {
            producer.send(messageSupplier.get());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void close() throws Exception {
        session.close();
    }
}
