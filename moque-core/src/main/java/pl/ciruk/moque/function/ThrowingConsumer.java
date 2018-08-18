package pl.ciruk.moque.function;

public interface ThrowingConsumer<T> {
    void accept(T t) throws Exception;
}
