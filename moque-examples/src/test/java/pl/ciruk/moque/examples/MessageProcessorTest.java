package pl.ciruk.moque.examples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import pl.ciruk.moque.jms.JmsMoque;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MessageProcessorTest {

    @RegisterExtension
    static SpringExtension springExtension = new SpringExtension();

    @RegisterExtension
    static JmsMoque jmsMoque = JmsMoque.withConnectionSupplier(() -> springExtension.getConnection());

    @Test
    void shouldName() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        jmsMoque.whenReceived("responseQueue")
                .thenDo(countDownLatch::countDown)
                .thenConsume(textMessage -> System.out.println("Response: " + textMessage.getText()));

        springExtension.getJmsTemplate().convertAndSend("mailbox", "First message");

        Assertions.assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
    }

    static class SpringExtension implements BeforeAllCallback, AfterAllCallback {

        private ConfigurableApplicationContext applicationContext;

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            ExtensionContext.Store store = context.getStore(ExtensionContext.Namespace.GLOBAL);
            ConfigurableApplicationContext applicationContext = store.get("applicationContext", ConfigurableApplicationContext.class);
            applicationContext.close();
        }

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
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
