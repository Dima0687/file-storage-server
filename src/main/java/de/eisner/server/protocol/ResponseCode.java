package de.eisner.server.protocol;

/// ## ResponseCode
///
/// Enumeration representing HTTP-like status codes used in server responses.
/// Each response code includes both a numeric code and a descriptive message.
///
/// **Response Codes:**
/// - **OK (200):** Successful operation
/// - **Bad Request (400):** Malformed or invalid request
/// - **Forbidden (403):** Operation not allowed (e.g., file already exists)
/// - **Not Found (404):** Requested resource doesn't exist
/// - **Internal Error (500):** Server-side error during operation
public enum ResponseCode {
    /// Successful operation response code
    OK(200, "OK"),

    /// Bad or malformed request response code
    BAD_REQUEST(400, "Bad Request"),

    /// Requested resource not found response code
    NOT_FOUND(404, "Not Found"),

    /// Operation forbidden response code (e.g., permission denied)
    FORBIDDEN(403, "Forbidden"),

    /// Internal server error response code
    INTERNAL_ERROR(500, "Internal Server Error");

    /// The numeric HTTP-like status code
    public final int code;

    /// The descriptive message associated with the status code
    public final String message;

    /// Constructs a ResponseCode with the specified code and message.
    ///
    /// @param code The numeric status code
    /// @param message The descriptive message
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /// Formats the response code with optional data into a string representation.
    ///
    /// **Format:**
    /// - If data is null: returns just the code as a string
    /// - If data is provided: returns "{code} {data}"
    ///
    /// @param data Optional additional data to include in the response
    /// @return The formatted response string
    public String format(String data) {
        return data == null ? String.valueOf(code) : code + " " + data;
    }
}
