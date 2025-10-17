package dev.shoangenes.server;

import dev.shoangenes.config.ServerProperties;
import dev.shoangenes.exception.DatabaseException;
import dev.shoangenes.repository.DatabaseManager;
import dev.shoangenes.utils.LoggerUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * FileServer class that handles incoming file requests using a thread pool.
 * It listens to a specified host and port, accepts connections, and processes
 * requests using the RequestHandler class.
 */
class FileServer {
    /*======================= Fields =======================*/

    private final ExecutorService threadPool;
    private final ServerSocket serverSocket;
    private volatile boolean running = false;

    /*======================= Logger =======================*/

    Logger logger = LoggerUtil.getLogger(FileServer.class);

    /*======================= Constructors =======================*/

    /**
     * Private constructor to initialize the FileServer.
     * Sets up the server socket and thread pool based on configuration.
     *
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    FileServer() throws IOException {
        logger.info("Initializing File Server...");

        int configuredThreads = ServerProperties.getInstance().getThreadPoolSize();
        int availableCores = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Math.min(configuredThreads, availableCores));

        int port = ServerProperties.getInstance().getPort();
        String host = ServerProperties.getInstance().getHost();
        int backlog = ServerProperties.getInstance().getBacklog();
        serverSocket = new ServerSocket(port, backlog, InetAddress.getByName(host));
        running = true;

        logger.info("File Server initialized on " + ServerProperties.getInstance().getHost() + ":" + ServerProperties.getInstance().getPort());
    }

    /*======================= Private Methods =======================*/

    /**
     /**
     * Private method that starts the main loop to accept incoming connections.
     * While the server is running and the socket is not closed, it accepts new connections
     * and delegates them to a RequestHandler using the thread pool.
     * Handles I/O and database exceptions, logging errors and stopping the loop if the server is shutting down.
     */
    private void acceptConnections() {
        logger.info("Accepting connections...");
        while (running && !serverSocket.isClosed()) {
            try {
                threadPool.submit(new RequestHandler(serverSocket.accept(), new FileService()));
            } catch (IOException | DatabaseException e) {
                if (!running) {
                    logger.info("Server is shutting down, stopping acceptance of new connections.");
                    break;
                }
                logger.severe("Error accepting connection: " + e.getMessage());
            }
        }
    }

    /*====================== Public Methods =======================*/

    /**
     * Checks if the server is currently running.
     * @return true if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Gets the current status of the server.
     * @return 0 if stopped, 1 if running, 2 if terminated.
     */
    public int getStatus() {
        if (!running) return 0; // Stopped
        if (threadPool.isTerminated()) return 2; // Terminated
        return 1; // Running
    }

    /**
     * Gets the number of active connections being handled by the server.
     * @return the number of active connections.
     */
    public int getActiveConnections() {
        if (threadPool instanceof ThreadPoolExecutor executor) {
            return executor.getActiveCount();
        }
        return 0;
    }

    /**
     * Starts the FileServer in a new thread, allowing it to accept incoming connections.
     */
    public void start() {
        logger.info("Starting File Server...");
        new Thread(this::acceptConnections).start();
    }

    /**
     * Stops the FileServer, closes the server socket, shuts down the thread pool,
     * and releases all resources. Ensures a graceful shutdown of the server and
     * database connections.
     */
    public void stop() {
        logger.info("Stopping File Server...");
        running = false;
        try {
            serverSocket.close();
            logger.info("Server socket closed.");
        } catch (IOException e) {
            logger.severe("Error closing server socket: " + e.getMessage());
        }
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("Forcing shutdown of thread pool...");
                threadPool.shutdownNow();
            }
            logger.info("Thread pool shut down.");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        DatabaseManager.getInstance().close();
        logger.info("File Server stopped.");
    }
}