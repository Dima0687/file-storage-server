package de.eisner.server.exception;

import de.eisner.server.protocol.ResponseCode;

/// ## CommandException
///
/// Abstract base class for all command execution exceptions.
/// Provides a mechanism to map exceptions to appropriate HTTP-like response codes.
///
/// **Design:** Subclasses must implement getResponseCode() to define their
/// corresponding response codes, enabling automatic error handling in the server.
public abstract class CommandException extends Exception {

    /// Returns the ResponseCode that should be sent to the client when this exception occurs.
    ///
    /// @return The appropriate ResponseCode for this exception type
    public abstract ResponseCode getResponseCode();
}
