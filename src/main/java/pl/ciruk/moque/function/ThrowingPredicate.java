package pl.ciruk.moque.function;

public interface ThrowingPredicate<T> {
    boolean test(T t) throws Exception;
}
