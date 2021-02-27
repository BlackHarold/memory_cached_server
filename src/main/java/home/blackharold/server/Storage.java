package home.blackharold.server;

import home.blackharold.blacknour.model.Status;

public interface Storage extends AutoCloseable {

    Status put(String key, Long ttl, byte[] data);

    byte[] get(String key);

    Status remove(String key);

    Status clear();
}
