package de.eisner.server;

import de.eisner.server.connection.Session;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/// ## StorageServer
///
/// The main server class that listens for incoming client connections and manages
/// the file storage system. It initializes thread pools for handling client connections
/// and executing file operations concurrently.
///
/// **Key Responsibilities:**
/// - Accepts incoming client socket connections
/// - Manages file registry for file metadata persistence
/// - Delegates connection handling to Session objects via thread pool
/// - Coordinates client handler and task worker thread pools
///
/// **Thread Pools:**
/// - **CLIENT_HANDLER_POOL**: Handles incoming client connections
/// - **TASK_WORKER_POOL**: Executes file operation commands asynchronously
public class StorageServer {
    /// Logger instance for the StorageServer class
    private static final Logger LOGGER = Logger.getLogger(StorageServer.class.getName());

    /// Server binding address (localhost)
    private static final String ADDRESS = "127.0.0.1";

    /// Server listening port number
    private static final int PORT = 23456;

    /// Maximum number of concurrent client handler threads
    private static final int CLIENT_HANDLER_POOL_SIZE = 10;

    /// Maximum number of concurrent task worker threads for file operations
    private static final int TASK_WORKER_POOL_SIZE = 20;

    /// Main entry point for the StorageServer application.
    ///
    /// **Process Flow:**
    /// 1. Initializes file registry and thread pools
    /// 2. Creates server socket on specified address and port
    /// 3. Accepts incoming client connections in a loop
    /// 4. Creates Session objects to handle client interactions
    /// 5. Properly shuts down thread pools on termination
    ///
    /// @param args Command line arguments (currently unused)
    public static void main(String[] args) {
        Path storageDir = Path.of("src", "server", "data");
        Path registryFile = Path.of("src", "server", "data", "file_registry.dat");

        FileRegistry fileRegistry = new FileRegistry(registryFile);
        ExecutorService clientHandlerPool = Executors.newFixedThreadPool(CLIENT_HANDLER_POOL_SIZE);
        ExecutorService taskWorkerPool = Executors.newFixedThreadPool(TASK_WORKER_POOL_SIZE);

        try (
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS));
        ) {
            LOGGER.info("Server started!");
            LOGGER.info("Client-Handler Pool Size: " + CLIENT_HANDLER_POOL_SIZE);
            LOGGER.info("Task-Worker Pool Size: " + TASK_WORKER_POOL_SIZE);
            LOGGER.info("Server is running: %s:%s".formatted(ADDRESS, PORT));


            while (!server.isClosed()) {
                try {
                    Socket socket = server.accept();
                    Session session = new Session(socket, storageDir, server, fileRegistry, taskWorkerPool);
                    clientHandlerPool.execute(session);

                } catch (IOException e) {
                    if (server.isClosed()) {
                        LOGGER.info("Shutting down the server...");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        } finally {
            LOGGER.info("Shutting down Client-Handler Pool...");
            clientHandlerPool.shutdown();

            LOGGER.info("Shutting down Task-Worker Pool...");
            taskWorkerPool.shutdown();

            LOGGER.info("Server shutdown complete.");
        }
    }
}