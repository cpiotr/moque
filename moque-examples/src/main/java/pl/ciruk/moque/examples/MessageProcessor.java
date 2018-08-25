package pl.ciruk.moque.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {
    static final String QUEUE = "mailbox";
    static final String RESPONSE_QUEUE = "responseQueue";
    private final JmsTemplate jmsTemplate;

    @Autowired
    public MessageProcessor(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = QUEUE)
    public void receiveMessage(String text) {
        jmsTemplate.convertAndSend(RESPONSE_QUEUE, "Welp: " + text);
    }
}
