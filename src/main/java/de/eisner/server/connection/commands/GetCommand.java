package de.eisner.server.connection.commands;

import de.eisner.server.context.CommandContext;
import de.eisner.server.exception.CommandException;
import de.eisner.server.exception.FileNotFoundException;
import de.eisner.server.protocol.ResponseCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static de.eisner.server.connection.Sender.sendFileContent;
import static de.eisner.server.connection.Sender.sendResponse;

/// ## GetCommand
///
/// Handles file retrieval operations. Reads a file from storage and sends its
/// contents to the requesting client.
///
/// **Process:**
/// 1. Resolves the filename (by ID or by name)
/// 2. Validates the file path for security
/// 3. Reads the complete file content into memory
/// 4. Sends OK response followed by the file bytes
public class GetCommand implements Command {

    /// Executes the get command.
    ///
    /// @param ctx The command context
    /// @throws CommandException If retrieval fails
    @Override
    public void execute(CommandContext ctx) throws CommandException {
        String actualFileName = resolveFileName(ctx);
        Path filePath = validatePath(ctx.storageDir(), actualFileName);
        byte[] fileBytes = readFile(filePath);

        sendResponse(ctx.out(), ResponseCode.OK);
        sendFileContent(ctx.out(), fileBytes);
    }

    /// Reads the complete file content from the specified path.
    ///
    /// **Behavior:** Loads the entire file into memory. Suitable for files of
    /// reasonable size but may cause memory issues with very large files.
    ///
    /// @param filePath The path to the file to read
    /// @return The complete file content as a byte array
    /// @throws CommandException If reading fails
    private byte[] readFile(Path filePath) throws CommandException {
        try {
            return Files.readAllBytes(filePath);
        } catch (NoSuchFileException e) {
            throw new FileNotFoundException();
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