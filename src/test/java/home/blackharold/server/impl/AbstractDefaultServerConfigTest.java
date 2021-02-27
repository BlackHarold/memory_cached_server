package home.blackharold.server.impl;

import home.blackharold.server.Storage;

import java.util.Properties;

import static org.mockito.Mockito.mock;

public abstract class AbstractDefaultServerConfigTest {

    protected Storage storage;

    protected DefaultServerConfig createDefaultServerConfigMock(Properties overrideApplicationProperties) {
        storage = mock(Storage.class);
        return new DefaultServerConfig(overrideApplicationProperties) {
            @Override
            protected Storage createStorage() {
                return storage;
            }
        };
    }
}
