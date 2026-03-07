package de.eisner.server.exception;

import de.eisner.server.protocol.ResponseCode;

/// ## FileAlreadyExistsException
///
/// Exception thrown when attempting to create a file that already exists on the server.
/// Maps to HTTP 403 Forbidden response code.
public class FileAlreadyExistsException extends CommandException {

    /// Returns the response code for this exception (Forbidden/403).
    ///
    /// @return ResponseCode.FORBIDDEN
    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.FORBIDDEN;
    }
}
