package pl.ciruk.moque;

import org.junit.jupiter.api.Test;
import pl.ciruk.moque.function.ThrowingPredicate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WhenReceivedTest {
    @Test
    void shouldExecuteActionWhenPredicateMatches() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Gateway<String> gateway = mock(Gateway.class);
        ThrowingPredicate<String> predicate = mock(ThrowingPredicate.class);
        when(predicate.test(anyString())).thenReturn(true);
        var whenReceived = new WhenReceived<>(gateway, predicate)
                .thenDo(latch::countDown);

        whenReceived.onMessage("Test");

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
    }
}
