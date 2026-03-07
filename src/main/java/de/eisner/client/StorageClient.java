package de.eisner.client;

import de.eisner.client.request.RequestBuilder;
import de.eisner.client.ui.UserPrompts;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

import static de.eisner.client.protocol.ClientProtocol.*;

/// ## StorageClient
///
/// The main client application that establishes a connection to the file storage server
/// and provides an interactive command-line interface for file operations.
///
/// **Key Responsibilities:**
/// - Establishes and maintains TCP socket connection to the server
/// - Handles connection retry logic with exponential backoff
/// - Presents user menu for selecting operations (GET, PUT, DELETE)
/// - Sends commands to the server via RequestBuilder
/// - Manages asynchronous response receiving in dedicated threads
/// - Coordinates client shutdown
///
/// **Operation Flow:**
/// 1. Connects to server with automatic retries
/// 2. Displays action menu to user
/// 3. Builds and sends command to server
/// 4. Spawns Receiver thread to handle response
/// 5. Waits for response completion
/// 6. Repeats until user selects EXIT
///
/// **Thread Model:**
/// - Main thread: Handles user input and command dispatch
/// - Receiver threads: One per command for handling server responses
public class StorageClient {

    /// Logger instance for the StorageClient class
    private static final Logger LOGGER = Logger.getLogger(StorageClient.class.getName());

    /// Server binding address (localhost)
    private static final String ADDRESS = "127.0.0.1";

    /// Server listening port number
    private static final int PORT = 23456;

    /// Main entry point for the StorageClient application.
    ///
    /// **Process:**
    /// 1. Attempts to establish socket connection to server (with retries)
    /// 2. Sets up data streams for communication (input, output)
    /// 3. Creates Scanner for reading user input
    /// 4. Enters main command loop:
    ///    - Displays menu and gets user action
    ///    - Sends command to server
    ///    - Spawns Receiver thread to handle response
    ///    - Waits for response completion
    /// 5. Exits loop when user selects "EXIT"
    /// 6. Properly closes all resources
    ///
    /// **Exception Handling:** Logs and prints connection errors,
    /// thread interruptions, and IO exceptions.
    ///
    /// @param args Command line arguments (currently unused)
    public static void main(String[] args) {
        LOGGER.info("Client started!");

        try (
                Socket socket = createSocket();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                Scanner scanner = new Scanner(System.in);
        ) {
            LOGGER.info("Connected to server: %s:%s".formatted(ADDRESS, PORT));

            while (true) {
                try {
                    String actionSend = run(scanner, output);

                    Receiver receiver = new Receiver(input, actionSend);
                    Thread receiverThread = new Thread(receiver);

                    receiverThread.start();
                    receiverThread.join();

                    if ("EXIT".equals(actionSend)) {
                        break;
                    }

                } catch (InterruptedException e) {
                    LOGGER.warning("Thread was interrupted: " + e.getMessage());
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /// Creates and returns a socket connection to the server with automatic retry logic.
    ///
    /// **Retry Strategy:**
    /// - Maximum 10 connection attempts
    /// - 100ms delay between retries
    /// - Logs each failed attempt with retry count
    /// - Throws the last IOException encountered after all retries are exhausted
    ///
    /// **Error Handling:**
    /// - ConnectException: Server not ready, triggers retry
    /// - Other IOException: Also triggers retry
    /// - After max retries: Throws the last exception encountered
    ///
    /// @return A connected Socket to the server
    /// @throws InterruptedException If the retry sleep is interrupted
    /// @throws IOException If all connection attempts fail
    private static Socket createSocket() throws InterruptedException, IOException {
        int maxRetries = 10;
        IOException lastException = null;

        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                return new Socket(InetAddress.getByName(ADDRESS), PORT);
            } catch (ConnectException e) {
                lastException = e;
                LOGGER.info("Server not ready, retrying...(" + (retryCount + 1) + "/" + maxRetries + ")");

                if (retryCount < maxRetries - 1) {
                    Thread.sleep(100);
                }
            } catch (IOException e) {
                lastException = e;
            }
        }

        throw lastException;
    }

    /// Prompts the user to select an action and dispatches to the appropriate handler.
    ///
    /// **Actions:**
    /// - "1": Get a file (delegates to getAFile)
    /// - "2": Save/upload a file (delegates to saveAFile)
    /// - "3": Delete a file (delegates to deleteAFile)
    /// - "exit": Stop server and client (delegates to stopServer)
    /// - Other: Shows error and recursively calls run() to retry
    ///
    /// @param scanner The Scanner for reading user input
    /// @param out The DataOutputStream for sending commands
    /// @return The command that was executed (GET, PUT, DELETE, or EXIT)
    /// @throws IOException If communication with server fails
    private static String run(Scanner scanner, DataOutputStream out) throws IOException {
        UserPrompts prompts = new UserPrompts(scanner);
        String action = prompts.promptAction();

        return switch (action.toLowerCase()) {
            case "1" -> getAFile(scanner, out);
            case "2" -> saveAFile(scanner, out);
            case "3" -> deleteAFile(scanner, out);
            case "exit" -> stopServer(out);
            default -> {
                System.out.println("Invalid action! Please try again.");
                yield run(scanner, out);
            }
        };
    }

    /// Sends an EXIT command to the server to initiate shutdown.
    ///
    /// **Process:**
    /// 1. Builds an EXIT command using RequestBuilder
    /// 2. Sends it to the server
    /// 3. Returns the command name for response handling
    ///
    /// @param out The DataOutputStream to send the command through
    /// @return The EXIT command string
    /// @throws IOException If sending the command fails
    private static String stopServer(DataOutputStream out) throws IOException {
        new RequestBuilder(COMMAND_EXIT)
                .send(out);
        return COMMAND_EXIT;
    }

    /// Handles file upload (PUT) operation.
    ///
    /// **Process:**
    /// 1. Prompts user for local file name
    /// 2. Prompts user for server file name (empty = auto-generate)
    /// 3. Builds and sends PUT command with server filename
    /// 4. Uploads actual file bytes via FileService
    /// 5. Handles and logs any upload errors
    ///
    /// @param scanner The Scanner for reading user input
    /// @param out The DataOutputStream for sending command
    /// @return The PUT command string
    /// @throws IOException If command sending fails
    private static String saveAFile(Scanner scanner, DataOutputStream out) throws IOException {
        UserPrompts prompts = new UserPrompts(scanner);
        String localFileName = prompts.promptLocalFileName();
        String serverFileName = prompts.promptServerFileName();

        new RequestBuilder(COMMAND_PUT)
                .addParameter(serverFileName)
                .send(out);

        try {
            FileService.uploadFile(localFileName, out);
        } catch (IOException e) {
            System.out.println("Error uploading file: " + e.getMessage());
        }

        return COMMAND_PUT;
    }

    /// Handles file retrieval (GET) operation.
    ///
    /// **Process:**
    /// 1. Prompts user to choose search type (by name or by ID)
    /// 2. Prompts for the identifier (filename or numeric ID)
    /// 3. Builds and sends GET command with search type and identifier
    /// 4. Returns command name for response handling
    ///
    /// @param scanner The Scanner for reading user input
    /// @param out The DataOutputStream for sending command
    /// @return The GET command string
    /// @throws IOException If command sending fails
    private static String getAFile(Scanner scanner, DataOutputStream out) throws IOException {
        UserPrompts prompts = new UserPrompts(scanner);
        var choice = prompts.promptSearchType(COMMAND_GET);

        new RequestBuilder(COMMAND_GET)
                .addParameter(choice.searchType())
                .addParameter(choice.identifier())
                .send(out);

        return COMMAND_GET;
    }

    /// Handles file deletion (DELETE) operation.
    ///
    /// **Process:**
    /// 1. Prompts user to choose search type (by name or by ID)
    /// 2. Prompts for the identifier (filename or numeric ID)
    /// 3. Builds and sends DELETE command with search type and identifier
    /// 4. Returns command name for response handling
    ///
    /// @param scanner The Scanner for reading user input
    /// @param out The DataOutputStream for sending command
    /// @return The DELETE command string
    /// @throws IOException If command sending fails
    private static String deleteAFile(Scanner scanner, DataOutputStream out) throws IOException {
        UserPrompts prompts = new UserPrompts(scanner);
        var choice = prompts.promptSearchType(COMMAND_DELETE);

        new RequestBuilder(COMMAND_DELETE)
                .addParameter(choice.searchType())
                .addParameter(choice.identifier())
                .send(out);

        return COMMAND_DELETE;
    }
}
