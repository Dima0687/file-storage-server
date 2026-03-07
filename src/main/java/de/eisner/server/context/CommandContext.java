package de.eisner.server.context;

import de.eisner.server.FileRegistry;

import java.io.DataOutputStream;
import java.nio.file.Path;

/// ## CommandContext
///
/// A record that encapsulates all context information needed to execute a command.
/// Passed to command implementations to provide access to file data, output stream,
/// file registry, and storage directory without requiring constructor parameters.
///
/// **Design:** Serves as a data transfer object for command execution across thread boundaries.
///
/// @param fileBytes Raw file bytes for the operation (PUT operations)
/// @param out Output stream for sending responses to the client
/// @param identifier File identifier (filename or numeric ID)
/// @param content Additional content/data for the command
/// @param searchType Search type specification (by_id or by_name)
/// @param storageDir Path to the storage directory containing files
/// @param fileRegistry Reference to the file registry for ID/filename lookups
public record CommandContext(
        byte[] fileBytes,
        DataOutputStream out,
        String identifier,
        String content,
        String searchType,
        Path storageDir,
        FileRegistry fileRegistry
) {
}
