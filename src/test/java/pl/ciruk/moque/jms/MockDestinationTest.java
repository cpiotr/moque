package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.TextMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class MockDestinationTest {
    private static final String QUEUE_NAME = "Q1";

    @RegisterExtension
    static final MockDestination MOQUE = new MockDestination();

    @Test
    void shouldRegisterMultipleConsumersWithDifferentPredicates() {
        List<String> received = new ArrayList<>();
        List<String> messagesStartingWithOne = new ArrayList<>();
        MOQUE.whenReceived(QUEUE_NAME, message -> true)
                .thenConsume(message -> received.add(message.getText()));
        MOQUE.whenReceived(QUEUE_NAME, message -> message.getText().startsWith("1"))
                .thenConsume(textMessage -> messagesStartingWithOne.add(textMessage.getText()));

        MOQUE.send(QUEUE_NAME, "First");

        assertThat(received).containsExactly("First");
        assertThat(messagesStartingWithOne).isEmpty();
    }

    @Test
    void shouldRespondToDifferentQueue() {
        assertTimeout(Duration.ofSeconds(10), () -> {
            String responseQueue = "Q314";
            MOQUE.whenReceived(QUEUE_NAME, message -> true)
                    .thenSend(responseQueue, "Response");

            MOQUE.send(QUEUE_NAME, "Trigger");

            TextMessage message = MOQUE.receiveFrom(responseQueue);
            assertThat(message.getText()).isEqualTo("Response");
        });
    }
}