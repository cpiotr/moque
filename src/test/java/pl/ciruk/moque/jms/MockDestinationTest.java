package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

class MockDestinationTest {
    @RegisterExtension
    static MockDestination mockDestination = new MockDestination();

    @Test
    void shouldRunServer() throws InterruptedException {
        Thread.sleep(5_000);
    }
}