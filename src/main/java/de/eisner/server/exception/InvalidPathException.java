package de.eisner.server.exception;

import de.eisner.server.protocol.ResponseCode;

/// ## InvalidPathException
///
/// Exception thrown when a file path attempts to escape the designated storage directory.
/// This security check prevents directory traversal attacks (e.g., "../../../etc/passwd").
/// Maps to HTTP 403 Forbidden response code.
public class InvalidPathException extends CommandException {

    /// Returns the response code for this exception (Forbidden/403).
    ///
    /// @return ResponseCode.FORBIDDEN
    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.FORBIDDEN;
    }
}
