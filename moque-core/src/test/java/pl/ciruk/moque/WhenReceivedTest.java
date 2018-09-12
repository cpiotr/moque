package pl.ciruk.moque;

import org.junit.jupiter.api.Test;
import pl.ciruk.moque.function.ThrowingConsumer;
import pl.ciruk.moque.function.ThrowingPredicate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WhenReceivedTest {

    private static final String RESPONSE_QUEUE = "ResponseQueue";

    @Test
    void shouldExecuteActionWhenPredicateMatches() throws Exception {
        Runnable action = mock(Runnable.class);
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(true);
        var whenReceived = new WhenReceived<>(mockGateway(), predicate)
                .thenDo(action);

        whenReceived.onMessage("Test");

        verify(action).run();
    }

    @Test
    void shouldNotExecuteActionWhenPredicateDoesNotMatch() throws Exception {
        Runnable action = mock(Runnable.class);
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(false);
        var whenReceived = new WhenReceived<>(mockGateway(), predicate)
                .thenDo(action);

        whenReceived.onMessage("Test");

        verifyZeroInteractions(action);
    }

    @Test
    void shouldConsumeMessageWhenPredicateMatches() throws Exception {
        ThrowingConsumer<String> consumer = mock(ThrowingConsumer.class);
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(true);
        var whenReceived = new WhenReceived<>(mockGateway(), predicate)
                .thenConsume(consumer);
        String message = "Test";

        whenReceived.onMessage(message);

        verify(consumer).accept(message);
    }

    @Test
    void shouldNotConsumeMessageWhenPredicateDoesNotMatch() throws Exception {
        ThrowingConsumer<String> consumer = mock(ThrowingConsumer.class);
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(false);
        var whenReceived = new WhenReceived<>(mockGateway(), predicate)
                .thenConsume(consumer);
        String message = "Test";

        whenReceived.onMessage(message);

        verifyZeroInteractions(consumer);
    }

    @Test
    void shouldSendResponseWhenPredicateMatches() throws Exception {
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(true);
        Gateway<String> gateway = mockGateway();
        String response = "Response";
        var whenReceived = new WhenReceived<>(gateway, predicate)
                .thenSend(RESPONSE_QUEUE, response);

        whenReceived.onMessage("Test");

        verify(gateway).send(RESPONSE_QUEUE, response);
    }

    @Test
    void shouldNotSendResponseWhenPredicateDoesNotMatch() throws Exception {
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(false);
        Gateway<String> gateway = mockGateway();
        String response = "Response";
        var whenReceived = new WhenReceived<>(gateway, predicate)
                .thenSend(RESPONSE_QUEUE, response);

        whenReceived.onMessage("Test");

        verifyZeroInteractions(gateway);
    }

    @SuppressWarnings("unchecked")
    private static Gateway<String> mockGateway() {
        return mock(Gateway.class);
    }
}
