package de.eisner.server.exception;

import de.eisner.server.protocol.ResponseCode;

/// ## FileNotFoundException
///
/// Exception thrown when a requested file cannot be found on the server.
/// Maps to HTTP 404 Not Found response code.
public class FileNotFoundException extends CommandException {

    /// Returns the response code for this exception (Not Found/404).
    ///
    /// @return ResponseCode.NOT_FOUND
    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.NOT_FOUND;
    }
}
