package pl.ciruk.moque.jms;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import javax.jms.JMSException;
import javax.jms.TextMessage;

class TextMessageAssert extends AbstractAssert<TextMessageAssert, TextMessage> {
    private TextMessageAssert(TextMessage textMessage) {
        super(textMessage, TextMessageAssert.class);
    }

    static TextMessageAssert assertThat(TextMessage textMessage) {
        return new TextMessageAssert(textMessage);
    }

    TextMessageAssert hasText(String text) {
        try {
            Assertions.assertThat(super.actual.getText()).isEqualTo(text);
        } catch (JMSException e) {
            throw new AssertionError(e);
        }
        return this;
    }
}
