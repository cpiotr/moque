package pl.ciruk.moque.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {
    private final JmsTemplate jmsTemplate;

    @Autowired
    public MessageProcessor(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "mailbox")
    public void receiveMessage(String text) {
        System.out.println("Received: " + text);
        jmsTemplate.convertAndSend("responseQueue", "Welp: " + text);
    }
}
