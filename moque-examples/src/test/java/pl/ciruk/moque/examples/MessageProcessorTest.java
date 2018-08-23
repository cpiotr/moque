package pl.ciruk.moque.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import pl.ciruk.moque.jms.JmsMoque;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MessageProcessorTest {

    @RegisterExtension
    static SpringExtension springExtension = new SpringExtension();

    @RegisterExtension
    static JmsMoque jmsMoque = JmsMoque.withConnectionSupplier(() -> springExtension.getConnection());

    @Test
    void shouldRespondToAnotherQueue() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        jmsMoque.whenReceived("responseQueue")
                .thenConsume(textMessage -> System.out.println("Response: " + textMessage.getText()))
                .thenDo(countDownLatch::countDown);

        springExtension.getJmsTemplate().convertAndSend("mailbox", "First message");

        assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
    }

    static class SpringExtension implements BeforeAllCallback, AfterAllCallback {

        private ConfigurableApplicationContext applicationContext;

        @Override
        public void afterAll(ExtensionContext context) {
            ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
            ConfigurableApplicationContext applicationContext = store.get("applicationContext", ConfigurableApplicationContext.class);
            applicationContext.close();
        }

        @Override
        public void beforeAll(ExtensionContext context) {
            applicationContext = SpringApplication.run(Configuration.class);
            ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
            store.put("applicationContext", applicationContext);
        }

        public JmsTemplate getJmsTemplate() {
            return applicationContext.getBean(JmsTemplate.class);
        }

        public Connection getConnection() {
            try {
                return applicationContext.getBean(ConnectionFactory.class).createConnection();
            } catch (JMSException e) {
                throw new AssertionError(e);
            }
        }
    }

}
