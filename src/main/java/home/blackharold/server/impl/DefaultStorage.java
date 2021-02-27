package home.blackharold.server.impl;

import home.blackharold.server.ServerConfig;
import home.blackharold.server.Storage;
import home.blackharold.blacknour.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

public class DefaultStorage implements Storage {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultStorage.class);

    final Map<String, StorageItem> map;
    final ExecutorService executorService;
    final Runnable clearExpiredDataJob;

    DefaultStorage(ServerConfig serverConfig) {
        int clearDataIntervalInMs = serverConfig.getClearDataIntervalInMs();
        this.map = createMap();
        this.executorService = createClearExecutorService();
        this.clearExpiredDataJob = new ClearExpiredDataJob(map, clearDataIntervalInMs);
        this.executorService.submit(clearExpiredDataJob);
    }

    Map<String, StorageItem> createMap() {
        return new ConcurrentHashMap<>();
    }

    ExecutorService createClearExecutorService() {
        return Executors.newSingleThreadExecutor(createClearExpiredDataThreadFactory());
    }

    ThreadFactory createClearExpiredDataThreadFactory() {
        return r -> {
            Thread clearExpiredDataJobThread = new Thread(r, "ClearExpiredDataJobThread");
            clearExpiredDataJobThread.setPriority(Thread.MIN_PRIORITY);
            clearExpiredDataJobThread.setDaemon(true);
            return clearExpiredDataJobThread;
        };
    }

    @Override
    public Status put(String key, Long ttl, byte[] data) {
        StorageItem oldItem = map.put(key, new StorageItem(key, ttl, data));
        return oldItem == null ? Status.ADDED : Status.REPLACED;
    }

    @Override
    public byte[] get(String key) {
        StorageItem item = map.get(key);
        if (item == null || item.isExpired()) {
            return null;
        }
        return item.data;
    }

    @Override
    public Status remove(String key) {
        StorageItem item = map.remove(key);
        return item != null && !item.isExpired() ? Status.REMOVED : Status.NOT_FOUND;
    }

    @Override
    public Status clear() {
        map.clear();
        return Status.CLEANED;
    }

    @Override
    public void close() throws Exception {
        //Do nothing, Daemon threads destroy automatically.
    }

    static class ClearExpiredDataJob implements Runnable {
        private final Map<String, StorageItem> map;
        private final int clearDataIntervalInMs;

        public ClearExpiredDataJob(Map<String, StorageItem> map, int clearDataIntervalInMs) {
            this.map = map;
            this.clearDataIntervalInMs = clearDataIntervalInMs;
        }

        boolean isStopRunnable() {
            return Thread.interrupted();
        }

        void sleepClearExpiredDataJob() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(clearDataIntervalInMs);
        }

        @Override
        public void run() {
            LOG.debug("ClearExpiredDataJobThread started with interval {} ms", clearDataIntervalInMs);
            while (!isStopRunnable()) {
                LOG.trace("Invoke clear job");
                for (Map.Entry<String, StorageItem> entry : map.entrySet()) {
                    if (entry.getValue().isExpired()) {
                        StorageItem item = map.remove(entry.getKey());
                        LOG.debug("Removed expired storage item {}", item);
                    }
                }

                try {
                    sleepClearExpiredDataJob();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    static class StorageItem {
        private final String key;
        private final Long ttl;
        private final byte[] data;

        StorageItem(String key, Long ttl, byte[] data) {
            this.key = key;
            this.data = data;
            this.ttl = ttl != null ? ttl + System.currentTimeMillis() : null;
        }

        boolean isExpired() {
            return ttl != null && ttl.longValue() < System.currentTimeMillis();
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder("[").append(key).append("]=");
            if (data == null) {
                stringBuilder.append("null");
            } else {
                stringBuilder.append(data.length).append(" bytes");
            }

            if (ttl != null) {
                stringBuilder.append(" (").append(new Date(this.ttl.longValue())).append(") ");
            }
            return stringBuilder.toString();
        }
    }

}
