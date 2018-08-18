package pl.ciruk.moque.function;

import java.util.function.Function;

public interface ThrowingFunction<F,T> {
    T apply(F from) throws Exception;
}
