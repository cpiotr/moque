package pl.ciruk.moque.jms;

import pl.ciruk.moque.WhenReceived;

import javax.jms.MessageConsumer;

class GatewayConsumer<T> implements AutoCloseable {
    private final MessageConsumer messageConsumer;
    private final WhenReceived<T> whenReceived;

    GatewayConsumer(MessageConsumer messageConsumer, WhenReceived<T> whenReceived) {
        this.messageConsumer = messageConsumer;
        this.whenReceived = whenReceived;
    }

    public WhenReceived<T> getWhenReceived() {
        return whenReceived;
    }

    @Override
    public void close() throws Exception {
        messageConsumer.close();
    }
}
