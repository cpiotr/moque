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
    JmsMoque moque = JmsMoque.withEmbeddedServer();

    @Test
    void shouldRespondToMultipleQueuesWhenMatchingPredicates() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            moque.whenReceived(QUEUE_NAME)
                    .thenSend(FIRST_RESPONSE_QUEUE, "FirstResponse")
                    .thenSend(SECOND_RESPONSE_QUEUE,  "SecondResponse");
            moque.whenReceived(QUEUE_NAME, message -> message.getText().startsWith("Content"))
                    .thenSend(THIRD_RESPONSE_QUEUE,  "SecondResponse");

            moque.send(QUEUE_NAME, "Data");

            assertThat(moque.receive(FIRST_RESPONSE_QUEUE))
                    .hasText("FirstResponse");
            assertThat(moque.receive(SECOND_RESPONSE_QUEUE))
                    .hasText("SecondResponse");
            assertThat(moque.receiveWithTimeout(THIRD_RESPONSE_QUEUE, 1, TimeUnit.SECONDS))
                    .isNull();
        });
    }

    @Test
    void shouldRespondToDifferentQueue() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            moque.whenReceived(QUEUE_NAME)
                    .thenSend(FIRST_RESPONSE_QUEUE, "Response");

            moque.send(QUEUE_NAME, "Trigger");

            TextMessage message = moque.receive(FIRST_RESPONSE_QUEUE);
            assertThat(message).hasText("Response");
        });
    }

    @Test
    void shouldRespondToMultipleQueues() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            moque.whenReceived(QUEUE_NAME)
                    .thenSend(FIRST_RESPONSE_QUEUE, "FirstResponse");
            moque.whenReceived(QUEUE_NAME)
                    .thenSend(SECOND_RESPONSE_QUEUE, textMessage -> "SecondResponse to: " +  textMessage.getText());

            moque.send(QUEUE_NAME, "Trigger");

            assertThat(moque.receive(FIRST_RESPONSE_QUEUE))
                    .hasText("FirstResponse");
            assertThat(moque.receive(SECOND_RESPONSE_QUEUE))
                    .hasText("SecondResponse to: Trigger");
        });
    }

}