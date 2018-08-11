package pl.ciruk.moque;

import java.util.function.Function;

public interface ThrowingFunction<F,T> {
    T apply(F from) throws Exception;
}
