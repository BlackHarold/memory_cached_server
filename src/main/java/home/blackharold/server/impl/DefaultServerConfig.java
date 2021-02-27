package home.blackharold.server.impl;

import home.blackharold.server.ClientSocketHandler;
import home.blackharold.server.CommandHandler;
import home.blackharold.server.ServerConfig;
import home.blackharold.server.Storage;
import home.blackharold.blacknour.exception.NourConfigException;
import home.blackharold.blacknour.protocol.RequestConverter;
import home.blackharold.blacknour.protocol.ResponseConverter;
import home.blackharold.blacknour.protocol.impl.DefaultRequestConverter;
import home.blackharold.blacknour.protocol.impl.DefaultResponseConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

public class DefaultServerConfig implements ServerConfig {
    private final Properties applicationProperties = new Properties();
    private final RequestConverter requestConverter;
    private final ResponseConverter responseConverter;
    private final Storage storage;
    private final CommandHandler commandHandler;

    DefaultServerConfig(Properties overrideApplicationProperties) {
        loadApplicationProperties("server.properties");
        if (overrideApplicationProperties != null) {
            applicationProperties.putAll(overrideApplicationProperties);
        }

        requestConverter = createRequestConverter();
        responseConverter = createResponseConverter();
        storage = createStorage();
        commandHandler = createCommandHandler();
    }

    RequestConverter createRequestConverter() {
        return new DefaultRequestConverter();
    }

    ResponseConverter createResponseConverter() {
        return new DefaultResponseConverter();
    }

    Storage createStorage() {
        return new DefaultStorage(this);
    }

    CommandHandler createCommandHandler() {
        return new DefaultCommandHandler(this);
    }

    InputStream getClassPathResourceInputStream(String classPathResource) {
        return getClass().getClassLoader().getResourceAsStream(classPathResource);
    }

    void loadApplicationProperties(String classPathResource) {
        try (InputStream inputStream = getClassPathResourceInputStream(classPathResource)) {
            if (inputStream == null) {
                throw new NourConfigException("Classpath resource not found");
            } else {
                applicationProperties.load(inputStream);
            }
        } catch (IOException e) {
            throw new NourConfigException("Can't load application properties");
        }
    }

    @Override
    public RequestConverter getRequestConverter() {
        return requestConverter;
    }

    @Override
    public ResponseConverter getResponseConverter() {
        return responseConverter;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public ThreadFactory getWorkerThreadFactory() {
        return new ThreadFactory() {
            private int threadCount = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Worker-" + threadCount);
                threadCount++;
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @Override
    public int getClearDataIntervalInMs() {
        String value = applicationProperties.getProperty("storage.clear.data.interval.ms");
        try {
            int interval = Integer.parseInt(value);
            if (interval < 1000) {
                throw new NourConfigException("storage.clear.data.interval.ms should be >= 1000 ms");
            }
            return interval;
        } catch (NumberFormatException nfe) {
            throw new NourConfigException("storage.clear.data.interval.ms should be a number");
        }
    }

    @Override
    public int getServerPort() {
        String value = applicationProperties.getProperty("server.port");
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 65535) {
                throw new NourConfigException("server.port should be between 0 and 65535");
            }
            return port;
        } catch (NumberFormatException nfe) {
            throw new NourConfigException("storage.clear.data.interval.ms should be a number");
        }
    }

    int getThreadCount(String propertyName) {
        String value = applicationProperties.getProperty(propertyName);
        try {
            int threadCount = Integer.parseInt(value);
            if (threadCount < 1) {
                throw new NourConfigException(propertyName + " should be >= 1");
            }
            return threadCount;
        } catch (NumberFormatException nfe) {
            throw new NourConfigException(propertyName + " should be a number");
        }
    }

    @Override
    public int getThisThreadCount() {
        return getThreadCount("server.init.thread.count");
    }

    @Override
    public int getMaxThreadCount() {
        return getThreadCount("server.max.thread.count");
    }

    @Override
    public ClientSocketHandler buildClientSocketHandler(Socket clientSocket) {
        return new DefaultClientSocketHandler(clientSocket, this);
    }

    @Override
    public void close() throws Exception {
        storage.close();
    }

    @Override
    public String toString() {
        return String.format("DefaultServerConfig: port=%s, initThreadCount=%s, maxThreadCount=%s, clearDataInterval=%s",
                getServerPort(), getThisThreadCount(), getMaxThreadCount(), getClearDataIntervalInMs());
    }
}
