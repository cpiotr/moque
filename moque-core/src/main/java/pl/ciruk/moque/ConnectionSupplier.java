package pl.ciruk.moque;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.function.Supplier;

public interface ConnectionSupplier<T> extends Supplier<T>, BeforeAllCallback, AfterAllCallback {
    @Override
    default void afterAll(ExtensionContext context) {

    }

    @Override
    default void beforeAll(ExtensionContext context) {

    }
}
