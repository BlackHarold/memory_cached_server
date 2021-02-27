package home.blackharold.server.impl;

import home.blackharold.server.CommandHandler;
import home.blackharold.server.ServerConfig;
import home.blackharold.server.Storage;
import home.blackharold.blacknour.exception.NourRuntimeException;
import home.blackharold.blacknour.model.Command;
import home.blackharold.blacknour.model.Request;
import home.blackharold.blacknour.model.Response;
import home.blackharold.blacknour.model.Status;

public class DefaultCommandHandler implements CommandHandler {

    private final Storage storage;

    DefaultCommandHandler(ServerConfig serverConfig) {
        this.storage = serverConfig.getStorage();
    }

    @Override
    public Response handle(Request request) {
        Status status = null;
        byte[] data = null;
        if (request.getCommand() == Command.CLEAR) {
            status = storage.clear();
        } else if (request.getCommand() == Command.PUT) {
            status = storage.put(request.getKey(), request.getTtl(), request.getData());
        } else if (request.getCommand() == Command.REMOVE) {
            status = storage.remove(request.getKey());
        } else if (request.getCommand() == Command.GET) {
            data = storage.get(request.getKey());
            status = data == null ? Status.NOT_FOUND : Status.GOTTEN;
        } else {
            throw new NourRuntimeException("Unsupported command: " + request.getCommand());
        }
        return new Response(status, data);
    }
}
