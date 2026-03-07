package de.eisner.server.parser;

import java.io.DataInputStream;
import java.io.IOException;

/// ## CommandParser
///
/// Utility class for parsing raw command strings from clients into structured
/// ParsedCommand objects. Handles validation of command format and parameters.
///
/// **Supported Commands:**
/// - **PUT:** Upload a file with binary content
/// - **GET:** Retrieve a file by ID or name
/// - **DELETE:** Delete a file by ID or name
public class CommandParser {

    /// Parses a raw command string into a structured ParsedCommand object.
    ///
    /// **Command Formats:**
    /// - PUT filename (file bytes follow via DataInputStream)
    /// - GET search_type identifier
    /// - DELETE search_type identifier
    ///
    /// **Validation:**
    /// - Checks required parameters for each command type
    /// - Validates search type is either "by_id" or "by_name"
    /// - Throws IllegalArgumentException for invalid input
    ///
    /// @param rawInput The raw command string from the client
    /// @param in DataInputStream to read file bytes for PUT operations
    /// @return A ParsedCommand object with extracted command information
    /// @throws IOException If reading from the input stream fails
    /// @throws IllegalArgumentException If command format is invalid
    public static ParsedCommand parse(String rawInput, DataInputStream in) throws IOException {
        String[] parts = rawInput.split(" ", 4);
        String command = parts[0].toLowerCase();

        return switch (command) {
            case "put" -> {
                if (parts.length < 2) throw new IllegalArgumentException("PUT requires filename");
                int length = in.readInt();
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                yield new ParsedCommand(command, parts[1], null, bytes);
            }
            case "get", "delete" -> {
                if (parts.length < 3) throw new IllegalArgumentException("GET/DELETE require search type and identifier");
                String searchType = parts[1].toLowerCase();
                if (!"by_id".equals(searchType) && !"by_name".equals(searchType)) {
                    throw new IllegalArgumentException("Invalid search type");
                }
                yield new ParsedCommand(command, parts[2], searchType, null);
            }
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
    }
}