package pl.ciruk.moque.jms;

import pl.ciruk.moque.Gateway;

import javax.jms.*;

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
    public void send(String destination, String message) {
        try (MessageProducer producer = session.createProducer(session.createQueue(destination))) {
            producer.send(session.createTextMessage(message));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        session.close();
    }
}
