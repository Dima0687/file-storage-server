package de.eisner.server.connection.commands;

import de.eisner.server.context.CommandContext;
import de.eisner.server.exception.CommandException;
import de.eisner.server.exception.FileNotFoundException;
import de.eisner.server.protocol.ResponseCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.eisner.server.connection.Sender.sendResponse;

/// ## DeleteCommand
///
/// Handles file deletion operations. Removes files from the storage directory
/// and updates the file registry accordingly.
///
/// **Process:**
/// 1. Resolves the filename (by ID or by name)
/// 2. Validates the file path for security
/// 3. Deletes the file from disk
/// 4. Updates the file registry if deletion was by ID
/// 5. Sends success response to client
public class DeleteCommand implements Command {

    /// Executes the delete command.
    ///
    /// @param ctx The command context
    /// @throws CommandException If deletion fails
    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String actualFileName = resolveFileName(ctx);
        Path filePath = validatePath(ctx.storageDir(), actualFileName);
        deleteFile(filePath);

        if ("by_id".equalsIgnoreCase(ctx.searchType())) {
            int fileId = Integer.parseInt(ctx.identifier());
            ctx.fileRegistry().removeFile(fileId);
        }

        sendResponse(ctx.out(), ResponseCode.OK);
    }

    /// Deletes a file from the specified path.
    ///
    /// **Behavior:** Uses Files.deleteIfExists() to atomically delete the file.
    /// Throws FileNotFoundException if the file doesn't exist.
    ///
    /// @param filePath The path to the file to delete
    /// @throws CommandException If deletion fails
    private void deleteFile(Path filePath) throws CommandException {
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (!deleted) {
                throw new FileNotFoundException();
            }
        } catch (IOException e) {
            throw new CommandException() {
                @Override
                public ResponseCode getResponseCode() {
                    return ResponseCode.INTERNAL_ERROR;
                }
            };
        }
    }

}