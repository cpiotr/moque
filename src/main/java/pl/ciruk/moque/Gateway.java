package pl.ciruk.moque;

import java.util.concurrent.TimeUnit;

public interface Gateway<T> extends AutoCloseable {
    T receive(String destination);

    T receiveWithTimeout(String destination, long time, TimeUnit timeUnit);

    void send(String destination, String message);
}
