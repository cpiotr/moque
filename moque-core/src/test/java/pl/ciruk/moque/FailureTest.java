package pl.ciruk.moque;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pl.ciruk.moque.jms.JmsMoque;

@Disabled("Needs to be run manually")
class FailureTest {
    @ExtendWith(JmsMoque.class)
    @Test
    void shouldFailWhenExtensionRegisteredAutomatically() {
        // Do nothing
    }
}
