package home.blackharold.server.impl;

import home.blackharold.server.ClientSocketHandler;
import home.blackharold.server.CommandHandler;
import home.blackharold.server.ServerConfig;
import home.blackharold.blacknour.model.Request;
import home.blackharold.blacknour.model.Response;
import home.blackharold.blacknour.protocol.RequestConverter;
import home.blackharold.blacknour.protocol.ResponseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class DefaultClientSocketHandler implements ClientSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultClientSocketHandler.class);
    private final Socket socket;
    private final ServerConfig serverConfig;

    DefaultClientSocketHandler(Socket socket, ServerConfig serverConfig) {
        this.socket = socket;
        this.serverConfig = serverConfig;
    }

    boolean isStopRunnable() {
        return Thread.interrupted();
    }

    @Override
    public void run() {
        try {
            RequestConverter requestConverter = serverConfig.getRequestConverter();
            ResponseConverter responseConverter = serverConfig.getResponseConverter();
            CommandHandler commandHandler = serverConfig.getCommandHandler();
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            while (!isStopRunnable()) {
                try {
                    Request request = requestConverter.readRequest(inputStream);
                    Response response = commandHandler.handle(request);
                    responseConverter.writeResponse(outputStream, response);
                    LOG.debug("Command {} -> {}", request, response);
                } catch (RuntimeException re) {
                    LOG.error("Handle request failed: " + re.getMessage(), re);
                }
            }
        } catch (EOFException | SocketException e) {
            LOG.error("Remote client connection closed: "
                    + socket.getRemoteSocketAddress().toString() + ": " + e.getMessage(), e);
        } catch (IOException e) {
            if (socket.isClosed()) {
                LOG.error("Socket is closed: " + e.getMessage(), e);
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error("Can't close socket: " + e.getMessage(), e);
            }
        }

    }
}
