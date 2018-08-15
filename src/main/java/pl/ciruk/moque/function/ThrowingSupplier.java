package pl.ciruk.moque.function;

public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
