package home.blackharold.server.impl;

import home.blackharold.server.Server;
import home.blackharold.server.ServerConfig;
import home.blackharold.blacknour.exception.NourRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

class DefaultServer implements Server {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultServer.class);
    private final ServerConfig serverConfig;
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final Thread mainThreadServer;

    private volatile boolean serverStopped;

    public DefaultServer(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.serverSocket = createServerSocket();
        this.executorService = createExecutorService();
        this.mainThreadServer = createMainServerThread(createServerRunnable());
    }

    ServerSocket createServerSocket() {
        try {
            ServerSocket socket = new ServerSocket(serverConfig.getServerPort());
            socket.setReuseAddress(true);
            return socket;
        } catch (IOException e) {
            throw new NourRuntimeException("Can't create server socket with port=" + serverConfig.getServerPort());
        }
    }

    ExecutorService createExecutorService() {
        ThreadFactory threadFactory = serverConfig.getWorkerThreadFactory();
        int initThreadCount = serverConfig.getThisThreadCount();
        int maxThreadCount = serverConfig.getMaxThreadCount();
        return new ThreadPoolExecutor(
                initThreadCount, maxThreadCount, 60L,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    Thread createMainServerThread(Runnable r) {
        Thread thread = new Thread(r, "Main server thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(true);
        return thread;
    }

    Runnable createServerRunnable() {
        return () -> {
            while (!mainThreadServer.isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    try {
                        executorService.submit(serverConfig.buildClientSocketHandler(clientSocket));
                        LOG.info("A new client connection established: " + clientSocket.getRemoteSocketAddress().toString());
                    } catch (RejectedExecutionException ree) {
                        LOG.error("All worker threads are busy. A new connection rejected: " + ree.getMessage(), ree);
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        LOG.error("Can't accept client socket: " + e.getMessage(), e);
                    }
                    destroyServer();
                    break;
                }
            }
        };
    }

    Thread getShoutDownHook() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (!serverStopped) {
                    destroyServer();
                }
            }
        }, "ShutDownHook");
    }

    void destroyServer() {
        try {
            serverConfig.close();
        } catch (Exception e) {
            LOG.error("Can't closing config: " + e.getMessage(), e);
        }
        executorService.shutdownNow();
        LOG.info("Server stopped");
    }

    @Override
    public void start() {

        if (mainThreadServer.getState() != Thread.State.NEW) {
            throw new NourRuntimeException("Current server already started!");
        }

        Runtime.getRuntime().addShutdownHook(getShoutDownHook());
        mainThreadServer.start();
    }

    @Override
    public void stop() {
        LOG.info("Stop comment");
        mainThreadServer.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Error during close server socket: " + e.getMessage(), e);
        }
    }
}
