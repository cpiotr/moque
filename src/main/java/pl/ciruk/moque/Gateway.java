package pl.ciruk.moque;

import javax.jms.TextMessage;
import java.util.concurrent.TimeUnit;

public interface Gateway<T> extends AutoCloseable {
    T receive(String destination);

    TextMessage receiveWithTimeout(String destination, long time, TimeUnit timeUnit);

    void send(String destination, String message);
}
