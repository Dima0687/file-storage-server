package de.eisner.client.request;

import java.io.DataOutputStream;
import java.io.IOException;

/// ## RequestBuilder
///
/// Builder class for constructing and sending client requests to the server.
/// Provides a fluent interface for building command strings with parameters.
///
/// **Design:** Uses method chaining to allow readable request construction:
/// ```
/// new RequestBuilder("PUT")
///     .addParameter("filename")
///     .send(out);
/// ```
public class RequestBuilder {
    /// StringBuilder for accumulating the command string
    private final StringBuilder sb = new StringBuilder();

    /// Constructs a new RequestBuilder with the specified command.
    ///
    /// @param command The command name (GET, PUT, DELETE, EXIT)
    public RequestBuilder(String command) {
        this.sb.append(command);
    }

    /// Adds a parameter to the command string.
    ///
    /// **Behavior:**
    /// - If param is null: does nothing
    /// - If param is empty: adds a single space
    /// - If param is non-empty: adds space followed by the parameter
    ///
    /// @param param The parameter to add
    /// @return This RequestBuilder instance for method chaining
    public RequestBuilder addParameter(String param) {
        if (param != null && !param.isEmpty()) {
            sb.append(" ").append(param);
        } else if (param != null) {
            sb.append(" ");
        }
        return this;
    }

    /// Sends the constructed request to the server and flushes the output stream.
    ///
    /// **Behavior:** Logs confirmation that the request was sent.
    ///
    /// @param out The output stream to send the request to
    /// @throws IOException If writing to the output stream fails
    public void send(DataOutputStream out) throws IOException {
        out.writeUTF(sb.toString());
        out.flush();
        System.out.println("The request was sent.");
    }
}
