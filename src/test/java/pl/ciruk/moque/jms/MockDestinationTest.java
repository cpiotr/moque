package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.TextMessage;

class MockDestinationTest {
    @RegisterExtension
    static MockDestination mockDestination = new MockDestination();

    @Test
    void shouldRunServer() throws InterruptedException {
        mockDestination.whenReceived("Q1", message -> message instanceof TextMessage)
                .thenConsume(System.out::println);
        Thread.sleep(5_000);
    }
}