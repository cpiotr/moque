package pl.ciruk.moque.jms;

public interface Gateway<T> {
    void receive(String destination, T message);

    void send(String destination, String message);
}
