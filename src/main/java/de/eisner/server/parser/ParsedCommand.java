package de.eisner.server.parser;

/// ## ParsedCommand
///
/// A record that encapsulates the result of parsing a client command.
/// It contains all information extracted from the raw command string and
/// any associated binary data.
///
/// @param command The parsed command type (put, get, delete, etc.)
/// @param identifier The file identifier (filename or ID depending on context)
/// @param searchType How the file should be searched (by_id or by_name)
/// @param fileBytes The binary content of the file (for PUT operations)
public record ParsedCommand(
        String command,
        String identifier,
        String searchType,
        byte[] fileBytes
) {}
