package pl.ciruk.moque.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.ciruk.moque.jms.JmsMoque;

import javax.jms.ConnectionFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Configuration.class)
@ExtendWith(SpringExtension.class)
class MessageProcessorTest {

    private static final String ANOTHER_QUEUE = "AnotherQueue";

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private JmsTemplate jmsTemplate;

    @RegisterExtension
    JmsMoque jmsMoque = JmsMoque.withConnectionSupplier(() -> connectionFactory.createConnection());

    @Test
    void shouldExecuteActionWhenMessageReceived() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        jmsMoque.whenReceived(MessageProcessor.RESPONSE_QUEUE)
                .thenDo(countDownLatch::countDown);

        jmsTemplate.convertAndSend(MessageProcessor.QUEUE, "First message");

        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldRespondToAnotherQueue() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String secondMessage = "Second message";
        jmsMoque.whenReceived(MessageProcessor.RESPONSE_QUEUE)
                .thenSend(ANOTHER_QUEUE, secondMessage)
                .thenDo(countDownLatch::countDown);

        jmsTemplate.convertAndSend(MessageProcessor.QUEUE, "First message");

        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
        String anotherMessage = (String) jmsTemplate.receiveAndConvert(ANOTHER_QUEUE);
        assertThat(anotherMessage).isEqualTo(secondMessage);
    }
}
