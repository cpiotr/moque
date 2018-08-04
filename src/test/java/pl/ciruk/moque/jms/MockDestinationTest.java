package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.JMSException;
import javax.jms.TextMessage;

class MockDestinationTest {
    @RegisterExtension
    static MockDestination mockDestination = new MockDestination();

    @Test
    void shouldRunServer() throws InterruptedException {
        mockDestination.whenReceived("Q1", message -> message instanceof TextMessage)
                .thenConsume(textMessage -> {
                    try {
                        System.out.println(textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                })
                .thenSend("Q314", "Hi there");
        Thread.sleep(5_000);
    }
}