package pl.ciruk.moque;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
