package pl.ciruk.moque.jms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class JmsMoqueSingleQueueTest {
    private static final String QUEUE_NAME = "Q1";

    @RegisterExtension
    static final JmsMoque MOQUE = JmsMoque.withEmbeddedServer();

    @Test
    void shouldRegisterMultipleConsumersWithDifferentPredicates() {
        List<String> received = new ArrayList<>();
        List<String> messagesStartingWithOne = new ArrayList<>();
        MOQUE.whenReceived(QUEUE_NAME)
                .thenConsume(message -> received.add(message.getText()));
        MOQUE.whenReceived(QUEUE_NAME, message -> message.getText().startsWith("1"))
                .thenConsume(textMessage -> messagesStartingWithOne.add(textMessage.getText()));

        MOQUE.send(QUEUE_NAME, "First");

        assertThat(received).containsExactly("First");
        assertThat(messagesStartingWithOne).isEmpty();
    }

    @Test
    void shouldCountDownWhenReceived() {
        assertTimeout(Duration.ofSeconds(10), () -> {
            CountDownLatch latch = new CountDownLatch(1);
            MOQUE.whenReceived(QUEUE_NAME)
                    .thenDo(latch::countDown);

            MOQUE.send(QUEUE_NAME, "Trigger");

            assertThat(latch.await(100, TimeUnit.MILLISECONDS)).isTrue();
        });
    }
}
