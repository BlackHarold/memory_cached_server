package home.blackharold.server.impl;


import home.blackharold.server.ServerConfig;
import home.blackharold.server.Storage;
import home.blackharold.blacknour.exception.NourRuntimeException;
import home.blackharold.blacknour.model.Command;
import home.blackharold.blacknour.model.Request;
import home.blackharold.blacknour.model.Response;
import home.blackharold.blacknour.model.Status;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultCommandHandlerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private Storage storage;
    private ServerConfig serverConfig;
    private DefaultCommandHandler defaultCommandHandler;

    @Before
    public void before() {
        storage = mock(Storage.class);
        serverConfig = mock(ServerConfig.class);
        when(serverConfig.getStorage()).thenReturn(storage);
        defaultCommandHandler = new DefaultCommandHandler(serverConfig);

    }

    @Test
    public void handleClear() {
        when(storage.clear()).thenReturn(Status.CLEANED);
        Response response = defaultCommandHandler.handle(new Request(Command.CLEAR));
        assertEquals(Status.CLEANED, response.getStatus());
        assertNull(response.getData());
        verify(storage).clear();
    }

    @Test
    public void handlePut() {
        String key = "key";
        Long ttl = System.currentTimeMillis();
        byte[] data = {1, 2, 3};
        when(storage.put(key, ttl, data)).thenReturn(Status.ADDED);
        Response response = defaultCommandHandler.handle(new Request(Command.PUT, key, ttl, data));
        assertEquals(Status.ADDED, response.getStatus());
        assertNull(response.getData());
        verify(storage).put(key, ttl, data);
    }

    @Test
    public void handleRemove() {
        String key = "key";
        when(storage.remove(key)).thenReturn(Status.REMOVED);
        Response response = defaultCommandHandler.handle(new Request(Command.REMOVE, key));
        assertEquals(Status.REMOVED, response.getStatus());
        assertNull(response.getData());
        verify(storage).remove(key);
    }

    @Test
    public void handleGetNotFound() {
        String key = "key";
        when(storage.get(key)).thenReturn(null);
        Response response = defaultCommandHandler.handle(new Request(Command.GET, key));
        assertEquals(Status.NOT_FOUND, response.getStatus());
        assertNull(response.getData());
        verify(storage).get(key);
    }

    @Test
    public void handleGetFound() {
        String key = "key";
        byte[] data = {1, 2, 3};
        when(storage.get(key)).thenReturn(data);
        Response response = defaultCommandHandler.handle(new Request(Command.GET, key));
        assertEquals(Status.GOTTEN, response.getStatus());
        assertArrayEquals(data, response.getData());
        verify(storage).get(key);
    }

    @Test
    public void handleUnsupportedCommand() {
        thrown.expect(NourRuntimeException.class);
        thrown.expectMessage(is("Unsupported command: null"));
        defaultCommandHandler.handle(new Request(null));
    }

}
