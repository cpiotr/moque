package pl.ciruk.moque.jms;

import pl.ciruk.moque.WhenReceived;

import javax.jms.MessageConsumer;
import java.util.ArrayList;
import java.util.List;

class GatewayConsumer<T> implements AutoCloseable {
    private final MessageConsumer messageConsumer;
    private final List<WhenReceived<T>> whenReceivedPredicates = new ArrayList<>();

    GatewayConsumer(MessageConsumer messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    void addWhenReceivedPredicate(WhenReceived<T> whenReceived) {
        whenReceivedPredicates.add(whenReceived);
    }

    void onMessage(T message) {
        whenReceivedPredicates.forEach(whenReceived -> whenReceived.onMessage(message));
    }

    @Override
    public void close() throws Exception {
        messageConsumer.close();
    }
}
