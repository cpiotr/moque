package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.JMSException;
import javax.jms.TextMessage;

class MockDestinationTest {
    @RegisterExtension
    static MockDestination mockDestination = new MockDestination();

    @Test
    void shouldRunServer() throws JMSException {
        mockDestination.whenReceived("Q1", message -> message instanceof TextMessage)
                .thenConsume(textMessage -> System.out.println(textMessage.getText()))
                .thenSend("Q314", "Hi there");
        mockDestination.whenReceived("Q1", textMessage -> textMessage.getText().startsWith("f"))
                .thenConsume(textMessage -> System.out.println("Welp"));

        mockDestination.send("Q1", "First");

        TextMessage message = mockDestination.receiveFrom("Q314");
        System.out.println(message.getText());
    }

    @Test
    void shouldRunServer2() throws JMSException {
        mockDestination.whenReceived("Q1", message -> message instanceof TextMessage)
                .thenConsume(textMessage -> System.out.println(textMessage.getText()))
                .thenSend("Q314", "Hi there");
        mockDestination.whenReceived("Q1", textMessage -> textMessage.getText().startsWith("f"))
                .thenConsume(textMessage -> System.out.println("Welp"));

        mockDestination.send("Q1", "Second");

        TextMessage message = mockDestination.receiveFrom("Q314");
        System.out.println(message.getText());
    }
}