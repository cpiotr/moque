package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.TextMessage;

class MockDestinationTest {
    @RegisterExtension
    static MockDestination mockDestination = new MockDestination();

    @Test
    void shouldRunServer() {
        mockDestination.whenReceived("Q1", message -> message instanceof TextMessage)
                .thenConsume(textMessage -> System.out.println(textMessage.getText()))
                .thenSend("Q314", "Hi there");
        mockDestination.whenReceived("Q1", textMessage -> textMessage.getText().startsWith("f"))
                .thenConsume(textMessage -> System.out.println("Welp"));
    }
}