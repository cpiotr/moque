package pl.ciruk.moque.jms;

import org.junit.jupiter.api.extension.ExtensionContext;
import pl.ciruk.moque.ConnectionSupplier;

import javax.jms.Connection;

class EmbeddedConnectionSupplier implements ConnectionSupplier<Connection> {
    private final EmbeddedServer server = new EmbeddedServer();

    @Override
    public void beforeAll(ExtensionContext context) {
        server.start();
    }

    @Override
    public Connection get() {
        return server.createConnection();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        server.close();
    }
}
