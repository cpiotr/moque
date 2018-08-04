package pl.ciruk.moque;

public interface Gateway<T> {
    void receive(String destination, T message);

    void send(String destination, String message);
}
