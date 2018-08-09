package pl.ciruk.moque.jms;

import pl.ciruk.moque.ThrowingRunnable;
import pl.ciruk.moque.WhenReceived;

import java.util.ArrayList;
import java.util.List;

class GatewayConsumer<T> implements AutoCloseable {
    private final ThrowingRunnable closeActionHandler;
    private final List<WhenReceived<T>> whenReceivedPredicates = new ArrayList<>();

    GatewayConsumer(ThrowingRunnable closeActionHandler) {
        this.closeActionHandler = closeActionHandler;
    }

    void addWhenReceivedPredicate(WhenReceived<T> whenReceived) {
        whenReceivedPredicates.add(whenReceived);
    }

    void onMessage(T message) {
        whenReceivedPredicates.forEach(whenReceived -> whenReceived.onMessage(message));
    }

    @Override
    public void close() throws Exception {
        closeActionHandler.run();
    }
}
