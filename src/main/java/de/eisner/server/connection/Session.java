package de.eisner.server.connection;

import de.eisner.server.FileRegistry;
import de.eisner.server.connection.commands.Command;
import de.eisner.server.connection.commands.DeleteCommand;
import de.eisner.server.connection.commands.GetCommand;
import de.eisner.server.connection.commands.PutCommand;
import de.eisner.server.context.CommandContext;
import de.eisner.server.exception.CommandException;
import de.eisner.server.parser.CommandParser;
import de.eisner.server.parser.ParsedCommand;
import de.eisner.server.protocol.ResponseCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static de.eisner.server.connection.Sender.sendResponse;

/// ## Session
///
/// Represents a single client connection session. Implements Runnable to execute
/// on a dedicated thread from the client handler pool. Responsible for reading
/// client commands, parsing them, and delegating execution to command handlers.
///
/// **Lifecycle:**
/// 1. Accepts a Socket connection
/// 2. Reads UTF commands from the client
/// 3. Parses commands using CommandParser
/// 4. Submits command execution to the task worker pool
/// 5. Sends responses back to the client
/// 6. Terminates when client disconnects or "exit" command is received
public class Session implements Runnable {

    /// Logger instance for the Session class
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());

    /// Map of command names to Command implementations for dynamic dispatch
    private static final Map<String, Command> commands = Map.of(
            "put", new PutCommand(),
            "get", new GetCommand(),
            "delete", new DeleteCommand()
    );

    /// The client socket connection
    private final Socket socket;

    /// Path to the directory where files are stored
    private final Path storageDir;

    /// Reference to the server socket for shutdown operations
    private final ServerSocket server;

    /// Reference to the file registry for file metadata operations
    private final FileRegistry fileRegistry;

    /// Thread pool for executing file operation commands asynchronously
    private final ExecutorService taskWorkerPool;

    /// Constructs a new Session with all required dependencies.
    ///
    /// @param socket The client socket connection
    /// @param storageDir Path to the storage directory
    /// @param server Reference to the server socket
    /// @param fileRegistry Reference to the file registry
    /// @param taskWorkerPool Thread pool for executing commands
    public Session(Socket socket, Path storageDir, ServerSocket server, FileRegistry fileRegistry, ExecutorService taskWorkerPool) {
        this.socket = socket;
        this.storageDir = storageDir;
        this.server = server;
        this.fileRegistry = fileRegistry;
        this.taskWorkerPool = taskWorkerPool;
    }

    /// Main execution method for the session thread.
    ///
    /// **Process:**
    /// 1. Opens input/output streams to the client
    /// 2. Reads UTF-encoded commands in a loop
    /// 3. Handles special "exit" command by closing the server
    /// 4. Parses and validates commands
    /// 5. Submits valid commands to the task worker pool for asynchronous execution
    /// 6. Sends error responses for malformed commands
    /// 7. Terminates when client disconnects
    @Override
    public void run() {
        try (
                socket;
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        ) {
            LOGGER.info("Client connected: %s".formatted(socket.getInetAddress()));

            while (true) {
                String rawInput = in.readUTF();

                if ("exit".equalsIgnoreCase(rawInput)) {
                    sendResponse(out, ResponseCode.OK);
                    server.close();
                    break;
                }

                try {
                    ParsedCommand cmd = CommandParser.parse(rawInput, in);
                    CommandContext ctx = new CommandContext(
                            cmd.fileBytes(),
                            out,
                            cmd.identifier(),
                            null,
                            cmd.searchType(),
                            storageDir,
                            fileRegistry
                    );
                    taskWorkerPool.execute(() -> executeCommand(cmd.command(), ctx));
                } catch (IllegalArgumentException e) {
                    sendResponse(out, ResponseCode.BAD_REQUEST);
                }
            }

        } catch (IOException e) {
            LOGGER.info("Client disconnected: %s".formatted(socket.getInetAddress()));
        }
    }

    /// Executes a command by looking it up in the command map and invoking its execute method.
    ///
    /// **Error Handling:**
    /// - CommandException errors are mapped to appropriate response codes
    /// - Unexpected exceptions result in INTERNAL_ERROR response
    ///
    /// @param command The command name (put, get, delete)
    /// @param ctx The command context with all required information
    private void executeCommand(String command, CommandContext ctx) {
        try {
            commands.get(command).execute(ctx);
        } catch (CommandException e) {
            sendResponse(ctx.out(), e.getResponseCode());
        } catch (Exception e) {
            sendResponse(ctx.out(), ResponseCode.INTERNAL_ERROR);
        }
    }
}
