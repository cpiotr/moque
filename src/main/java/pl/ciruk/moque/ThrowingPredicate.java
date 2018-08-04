package pl.ciruk.moque;

public interface ThrowingPredicate<T> {
    boolean test(T t) throws Exception;
}
