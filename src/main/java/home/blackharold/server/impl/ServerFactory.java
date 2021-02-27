package home.blackharold.server.impl;

import home.blackharold.server.Server;

import java.util.Properties;

public class ServerFactory {

    public static Server buildServer(Properties overrideApplicationProperties) {
        return new DefaultServer(new DefaultServerConfig(overrideApplicationProperties));
    }
}
