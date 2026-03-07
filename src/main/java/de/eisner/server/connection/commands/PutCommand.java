package de.eisner.server.connection.commands;

import de.eisner.server.FileRegistry;
import de.eisner.server.context.CommandContext;
import de.eisner.server.exception.CommandException;
import de.eisner.server.exception.FileAlreadyExistsException;
import de.eisner.server.protocol.ResponseCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static de.eisner.server.connection.Sender.sendResponse;

/// ## PutCommand
///
/// Handles file upload operations. Receives file content from the client, stores it
/// on disk, and registers it in the file registry with a unique ID.
///
/// **Process:**
/// 1. Determines the filename (provided or auto-generated)
/// 2. Validates the file path for security
/// 3. Writes the file content to disk
/// 4. Registers the file in the registry
/// 5. Sends the assigned file ID back to the client
///
/// **Auto-Generated Filenames:** Format is "file_\[timestamp]_\[registry_size].bin"
public class PutCommand implements Command {

    /// Executes the put command.
    ///
    /// @param ctx The command context
    /// @throws CommandException If upload fails
    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String actualFileName = ctx.identifier().isEmpty() ?
                generateFileName(ctx.fileRegistry()) : ctx.identifier();

        Path filePath = validatePath(ctx.storageDir(), actualFileName);
        writeFile(filePath, ctx.fileBytes());

        long fileId = ctx.fileRegistry().registerFile(actualFileName);
        sendResponse(ctx.out(), ResponseCode.OK, String.valueOf(fileId));
    }

    /// Writes file content to disk with create-new semantics.
    ///
    /// **Behavior:** Uses StandardOpenOption.CREATE_NEW to ensure the file doesn't
    /// already exist, preventing accidental overwrites.
    ///
    /// @param filePath The path where the file should be written
    /// @param fileBytes The file content to write
    /// @throws FileAlreadyExistsException If the file already exists
    /// @throws CommandException If writing fails
    private void writeFile(Path filePath, byte[] fileBytes) throws CommandException {
        try {
            Files.write(filePath, fileBytes, StandardOpenOption.CREATE_NEW);
        } catch (java.nio.file.FileAlreadyExistsException e) {
            throw new FileAlreadyExistsException();
        } catch (IOException e) {
            throw new CommandException() {
                @Override
                public ResponseCode getResponseCode() {
                    return ResponseCode.INTERNAL_ERROR;
                }
            };
        }
    }

    /// Generates a unique filename based on timestamp and registry size.
    ///
    /// **Format:** "file_\[timestamp]_\[registry_size].bin"
    /// This provides a reasonable guarantee of uniqueness while being human-readable.
    ///
    /// @param fileRegistry Reference to the file registry for getting current size
    /// @return A generated filename
    private String generateFileName(FileRegistry fileRegistry) {
        long timestamp = System.currentTimeMillis();
        return "file_" + timestamp + "_" + fileRegistry.size() + ".bin";
    }
}
