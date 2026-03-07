package de.eisner.server.connection.commands;

import de.eisner.server.context.CommandContext;
import de.eisner.server.exception.CommandException;
import de.eisner.server.exception.FileNotFoundException;
import de.eisner.server.exception.InvalidPathException;

import java.nio.file.Path;

/// ## Command
///
/// Interface defining the contract for all file operation commands (GET, PUT, DELETE).
/// Provides common utility methods for path validation and filename resolution.
///
/// **Design:** Uses default methods to provide shared functionality while allowing
/// implementations to focus on command-specific logic.
public interface Command {

    /// Executes the command with the provided context.
    ///
    /// @param ctx The command context containing all required information
    /// @throws CommandException If the command execution fails
    void execute(CommandContext ctx) throws CommandException;

    /// Resolves the actual filename based on the search type and identifier.
    ///
    /// **Search Types:**
    /// - **by_id:** Looks up the filename using the file registry
    /// - **by_name:** Uses the identifier directly as the filename
    ///
    /// @param ctx The command context
    /// @return The resolved filename
    /// @throws FileNotFoundException If the ID doesn't exist in the registry
    /// @throws CommandException If resolution fails
    default String resolveFileName(CommandContext ctx) throws CommandException {
        if ("by_id".equalsIgnoreCase(ctx.searchType())) {
            try {
                int fieldId = Integer.parseInt(ctx.identifier());
                String fileName = ctx.fileRegistry().getFileName(fieldId);
                if (fileName == null) throw new FileNotFoundException();
                return fileName;
            } catch (NumberFormatException e) {
                throw new FileNotFoundException();
            }
        } else {
            return ctx.identifier();
        }
    }

    /// Validates that a resolved file path is within the designated storage directory.
    ///
    /// **Security:** Prevents directory traversal attacks by ensuring the normalized
    /// path stays within the storage directory boundaries.
    ///
    /// @param storageDir The designated storage directory
    /// @param fileName The filename to validate
    /// @return The validated absolute file path
    /// @throws InvalidPathException If the path attempts to escape the storage directory
    default Path validatePath(Path storageDir, String fileName) throws CommandException {
        Path filePath = storageDir.resolve(fileName).normalize();
        if (!filePath.toAbsolutePath().startsWith(storageDir.toAbsolutePath())) {
            throw new InvalidPathException();
        }
        return filePath;
    }
}
