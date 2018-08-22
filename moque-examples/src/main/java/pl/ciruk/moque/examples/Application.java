package pl.ciruk.moque.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jms.core.JmsTemplate;

public class Application {
    public static void main(String[] args) {
        // Launch the application
//        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Configuration.class);
        ConfigurableApplicationContext context = SpringApplication.run(Configuration.class, args);
//        context.refresh();
        JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);

        System.out.println("Sending an email message.");
        jmsTemplate.convertAndSend("mailbox", "Test");
    }
}
