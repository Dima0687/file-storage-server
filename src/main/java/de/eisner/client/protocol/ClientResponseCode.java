package de.eisner.client.protocol;

import static de.eisner.client.protocol.ClientProtocol.*;

/// ## ClientResponseCode
///
/// Enumeration representing HTTP-like response codes and messages used in
/// client-server communication. Maps response codes to appropriate user messages
/// based on the command that was executed.
///
/// **Response Codes:**
/// - **OK (200):** Successful operation
/// - **Bad Request (400):** Malformed request
/// - **Forbidden (403):** Operation not allowed
/// - **Not Found (404):** File not found
/// - **Internal Error (500):** Server-side error
/// - **EXIT (200):** Client and server shutdown
public enum ClientResponseCode {

    /// Successful GET response message
    OK_GET(200, "The File was downloaded! Specify a name for it: "),

    /// Successful PUT response message prefix (file ID is appended)
    OK_PUT(200, "Response says that file is saved! ID = "),

    /// Successful DELETE response message
    OK_DELETE(200, "The response says that this file was deleted successfully!"),

    /// Bad request error message
    BAD_REQUEST(400, "Invalid request!"),

    /// Forbidden operation error message
    FORBIDDEN(403, "The response says that creating the file was forbidden!"),

    /// File not found error message
    NOT_FOUND(404, "The response says that this file is not found!"),

    /// Internal server error message
    INTERNAL_ERROR(500, "Some server specific error occurs"),

    /// Shutdown confirmation message
    EXIT(200, "Client and Server stopped!");

    /// The HTTP-like numeric response code
    public final int code;

    /// The user-facing message for this response code
    public final String message;

    /// Constructs a ClientResponseCode with the specified code and message.
    ///
    /// @param code The numeric response code
    /// @param message The user-facing message
    ClientResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /// Retrieves the appropriate message for a given action and response code.
    ///
    /// **Behavior:** Uses nested switch expressions to map the action and response
    /// code to the appropriate message. Throws IllegalStateException for unknown codes.
    ///
    /// @param action The command that was sent (GET, PUT, DELETE, EXIT)
    /// @param code The response code from the server
    /// @param data Optional additional data (e.g., file ID for PUT responses)
    /// @return The appropriate message to display to the user
    /// @throws IllegalStateException If the code is unknown for the given action
    public static String getMessage(String action, int code, String data) {
        String unknownCodeMessage = "Unknown code: " + code;
        return switch (action) {
            case COMMAND_GET -> switch (code) {
                case 200 -> OK_GET.message;
                case 400 -> BAD_REQUEST.message;
                case 404 -> NOT_FOUND.message;
                case 500 -> INTERNAL_ERROR.message;
                default -> throw new IllegalStateException(unknownCodeMessage);
            };
            case COMMAND_PUT -> switch (code) {
                case 200 -> data != null ? OK_PUT.message + data : "The response says that the file was created!";
                case 400 -> BAD_REQUEST.message;
                case 403 -> FORBIDDEN.message;
                case 500 -> INTERNAL_ERROR.message;
                default -> throw new IllegalStateException(unknownCodeMessage);
            };
            case COMMAND_DELETE -> switch (code) {
                case 200 -> OK_DELETE.message;
                case 400 -> BAD_REQUEST.message;
                case 404 -> NOT_FOUND.message;
                case 500 -> INTERNAL_ERROR.message;
                default -> throw new IllegalStateException(unknownCodeMessage);
            };
            case COMMAND_EXIT -> EXIT.message;
            default -> throw new IllegalStateException("Unknown action: " + action);
        };
    }
}
