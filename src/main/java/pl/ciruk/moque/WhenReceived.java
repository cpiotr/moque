package pl.ciruk.moque;

import pl.ciruk.moque.function.ThrowingConsumer;
import pl.ciruk.moque.function.ThrowingFunction;
import pl.ciruk.moque.function.ThrowingPredicate;

import java.util.ArrayList;
import java.util.List;

public class WhenReceived<T> {
    private final Gateway<T> gateway;

    private final ThrowingPredicate<T> messageMatcher;

    private final List<ThrowingConsumer<T>> consumers = new ArrayList<>();

    public WhenReceived(Gateway<T> mockDestination, ThrowingPredicate<T> messageMatcher) {
        this.gateway = mockDestination;
        this.messageMatcher = messageMatcher;
    }

    public WhenReceived<T> thenSend(String destination, String message) {
        consumers.add(__ -> gateway.send(destination, message));
        return this;
    }

    public WhenReceived<T> thenSend(String destination, ThrowingFunction<T, String> messageOperator) {
        consumers.add(message -> gateway.send(destination, messageOperator.apply(message)));
        return this;
    }

    public WhenReceived<T> thenDoNothing() {
        return thenDo(() -> {});
    }

    public WhenReceived<T> thenDo(Runnable runnable) {
        consumers.add(__ -> runnable.run());
        return this;
    }

    public WhenReceived<T> thenConsume(ThrowingConsumer<T> messageConsumer) {
        consumers.add(messageConsumer);
        return this;
    }

    public void onMessage(T message) {
        try {
            if (messageMatcher.test(message)) {
                consumers.forEach(consumer -> {
                    try {
                        consumer.accept(message);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                });
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
