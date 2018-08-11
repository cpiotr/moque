package pl.ciruk.moque;

import pl.ciruk.moque.function.ThrowingRunnable;

import java.util.ArrayList;
import java.util.List;

public class GatewayConsumer<T> implements AutoCloseable {
    private final ThrowingRunnable closeActionHandler;
    private final List<WhenReceived<T>> whenReceivedPredicates = new ArrayList<>();

    public GatewayConsumer(ThrowingRunnable closeActionHandler) {
        this.closeActionHandler = closeActionHandler;
    }

    public void addWhenReceivedRule(WhenReceived<T> whenReceived) {
        whenReceivedPredicates.add(whenReceived);
    }

    public void onMessage(T message) {
        whenReceivedPredicates.forEach(whenReceived -> whenReceived.onMessage(message));
    }

    @Override
    public void close() throws Exception {
        closeActionHandler.run();
    }
}
