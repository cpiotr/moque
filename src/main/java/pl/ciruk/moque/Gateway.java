package pl.ciruk.moque;

public interface Gateway<T> extends AutoCloseable {
    T receive(String destination);

    void send(String destination, String message);
}
