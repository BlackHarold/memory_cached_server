package home.blackharold.server;

import home.blackharold.server.impl.ServerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServiceWrapper {

    private static final Server server = createServer();

    private static Server createServer() {
        return ServerFactory.buildServer(getServerProperties());
    }

    private static Properties getServerProperties() {
        Properties properties = new Properties();
        String pathToServerProperties = System.getProperty("server_properties");
        try (InputStream inputStream = new FileInputStream(pathToServerProperties)) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void main(String[] args) {
        if ("start".equals(args[0])) {
            start(args);
        } else {
            stop(args);
        }
    }

    public static void start(String... args) {
        server.start();
    }

    public static void stop(String... args) {
        server.stop();
    }
}

