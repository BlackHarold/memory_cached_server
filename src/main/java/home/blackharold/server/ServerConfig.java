package home.blackharold.server;

import home.blackharold.blacknour.protocol.RequestConverter;
import home.blackharold.blacknour.protocol.ResponseConverter;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

public interface ServerConfig extends AutoCloseable {

    RequestConverter getRequestConverter();

    ResponseConverter getResponseConverter();

    Storage getStorage();

    CommandHandler getCommandHandler();

    ThreadFactory getWorkerThreadFactory();

    int getClearDataIntervalInMs();

    int getServerPort();

    int getThisThreadCount();

    int getMaxThreadCount();

    ClientSocketHandler buildClientSocketHandler(Socket clientSocket);

}
