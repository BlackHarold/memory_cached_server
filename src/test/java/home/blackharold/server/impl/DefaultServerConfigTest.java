package home.blackharold.server.impl;

import home.blackharold.server.ClientSocketHandler;
import home.blackharold.blacknour.exception.NourConfigException;
import home.blackharold.blacknour.protocol.impl.DefaultRequestConverter;
import home.blackharold.blacknour.protocol.impl.DefaultResponseConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class DefaultServerConfigTest extends AbstractDefaultServerConfigTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private DefaultServerConfig defaultServerConfig;

    @Before
    public void before() {
        defaultServerConfig = createDefaultServerConfigMock(null);
    }

    @Test
    public void testDefaultInitState() throws Exception {
        try (DefaultServerConfig defaultServerConfig = new DefaultServerConfig(null)) {
            assertEquals(DefaultRequestConverter.class, defaultServerConfig.getRequestConverter().getClass());
            assertEquals(DefaultResponseConverter.class, defaultServerConfig.getResponseConverter().getClass());
            assertEquals(DefaultStorage.class, defaultServerConfig.getStorage().getClass());
            assertEquals(DefaultCommandHandler.class, defaultServerConfig.getCommandHandler().getClass());

            assertEquals(25666, defaultServerConfig.getServerPort());
            assertEquals(1, defaultServerConfig.getThisThreadCount());
            assertEquals(10, defaultServerConfig.getMaxThreadCount());
            assertEquals(10000, defaultServerConfig.getClearDataIntervalInMs());
        }
    }

    @Test
    public void getWorkerThreadFactory() {
        ThreadFactory threadFactory = defaultServerConfig.getWorkerThreadFactory();
        Thread thread = threadFactory.newThread(mock(Runnable.class));
        assertTrue(thread.isDaemon());
        assertEquals("Worker-0", thread.getName());
    }

    @Test
    public void close() throws Exception {
        defaultServerConfig.close();
        verify(storage).close();
    }

    @Test
    public void buildNewClientSocketHandler() {
        ClientSocketHandler clientSocketHandler = defaultServerConfig.buildClientSocketHandler(mock(Socket.class));
        assertEquals(DefaultClientSocketHandler.class, clientSocketHandler.getClass());
    }

    @Test
    public void verifyToString() {
        assertEquals("DefaultServerConfig: port=25666, initThreadCount=1, maxThreadCount=10, clearDataInterval=10000", defaultServerConfig.toString());
    }
}
