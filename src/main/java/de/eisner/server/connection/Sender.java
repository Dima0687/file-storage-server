package de.eisner.server.connection;

import de.eisner.server.protocol.ResponseCode;

import java.io.DataOutputStream;
import java.io.IOException;

/// ## Sender
///
/// Utility class for sending responses and file content back to clients.
/// Handles serialization of response codes, messages, and binary file data.
///
/// **Design:** Static utility methods provide a clean interface for sending data
/// with graceful handling of I/O errors.
public class Sender {

    /// Private constructor to prevent instantiation of this utility class
    private Sender() {
    }

    /// Sends a response code to the client without additional data.
    ///
    /// @param out The output stream to send the response to
    /// @param code The response code to send
    public static void sendResponse(DataOutputStream out, ResponseCode code) {
        sendResponse(out, code, null);
    }

    /// Sends a response code with optional additional data to the client.
    ///
    /// **Format:** Sends the code formatted with optional data as a UTF string.
    /// Flushes the output stream after writing.
    ///
    /// @param out The output stream to send the response to
    /// @param code The response code to send
    /// @param data Optional additional data to include in the response
    public static void sendResponse(DataOutputStream out, ResponseCode code, String data) {
        try {
            out.writeUTF(code.format(data));
            out.flush();
        } catch (IOException _) {
        }
    }

    /// Sends file content (binary data) to the client.
    ///
    /// **Format:**
    /// 1. First sends the file size as an integer
    /// 2. Then sends the raw file bytes
    /// 3. Flushes the output stream
    ///
    /// @param out The output stream to send the file to
    /// @param fileBytes The binary file content to send
    public static void sendFileContent(DataOutputStream out, byte[] fileBytes) {
        try {
            out.writeInt(fileBytes.length);
            out.write(fileBytes);
            out.flush();
        } catch (IOException _) {

        }
    }
}

