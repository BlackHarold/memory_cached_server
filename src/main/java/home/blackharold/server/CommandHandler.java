package home.blackharold.server;

import home.blackharold.blacknour.model.Request;
import home.blackharold.blacknour.model.Response;

public interface CommandHandler {

    Response handle(Request request);
}
