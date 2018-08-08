package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.jms.TextMessage;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static pl.ciruk.moque.jms.TextMessageAssert.assertThat;

class JmsMoqueMultipleQueueTest {
    private static final String QUEUE_NAME = "Q1";
    private static final String FIRST_RESPONSE_QUEUE = "FirstResponseQueue";
    private static final String SECOND_RESPONSE_QUEUE = "SecondResponseQueue";
    private static final String THIRD_RESPONSE_QUEUE = "SecondResponseQueue";

    @RegisterExtension
    static final JmsMoque MOQUE = new JmsMoque();

    @Test
    void shouldRespondToMultipleQueuesWhenMatchingPredicates() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            MOQUE.whenReceived(QUEUE_NAME, message -> true)
                    .thenSend(FIRST_RESPONSE_QUEUE, "FirstResponse")
                    .thenSend(SECOND_RESPONSE_QUEUE,  "SecondResponse");
            MOQUE.whenReceived(QUEUE_NAME, message -> message.getText().startsWith("Content"))
                    .thenSend(THIRD_RESPONSE_QUEUE,  "SecondResponse");

            MOQUE.send(QUEUE_NAME, "Data");

            assertThat(MOQUE.receive(FIRST_RESPONSE_QUEUE))
                    .hasText("FirstResponse");
            assertThat(MOQUE.receive(SECOND_RESPONSE_QUEUE))
                    .hasText("SecondResponse");
            assertThat(MOQUE.receiveWithTimeout(THIRD_RESPONSE_QUEUE, 1, TimeUnit.SECONDS))
                    .isNull();
        });
    }

    @Test
    void shouldRespondToDifferentQueue() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            MOQUE.whenReceived(QUEUE_NAME, message -> true)
                    .thenSend(FIRST_RESPONSE_QUEUE, "Response");

            MOQUE.send(QUEUE_NAME, "Trigger");

            TextMessage message = MOQUE.receive(FIRST_RESPONSE_QUEUE);
            assertThat(message).hasText("Response");
        });
    }

    @Test
    void shouldRespondToMultipleQueues() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            MOQUE.whenReceived(QUEUE_NAME, message -> true)
                    .thenSend(FIRST_RESPONSE_QUEUE, "FirstResponse");
            MOQUE.whenReceived(QUEUE_NAME, message -> true)
                    .thenSend(SECOND_RESPONSE_QUEUE, "SecondResponse");

            MOQUE.send(QUEUE_NAME, "Trigger");

            assertThat(MOQUE.receive(FIRST_RESPONSE_QUEUE))
                    .hasText("FirstResponse");
            assertThat(MOQUE.receive(SECOND_RESPONSE_QUEUE))
                    .hasText("SecondResponse");
        });
    }

}