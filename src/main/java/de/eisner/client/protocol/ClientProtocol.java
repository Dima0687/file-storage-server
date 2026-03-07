package de.eisner.client.protocol;

/// ## ClientProtocol
///
/// Constant definitions for client-side protocol elements.
/// Defines all command types and search type constants used in client-server communication.
///
/// **Commands:**
/// - GET: Retrieve a file
/// - PUT: Upload a file
/// - DELETE: Delete a file
/// - EXIT: Shut down client and server
///
/// **Search Types:**
/// - BY_ID: Search files by numeric ID
/// - BY_NAME: Search files by filename
public class ClientProtocol {

    /// Private constructor to prevent instantiation of this constants class
    private ClientProtocol() {}

    /// Constant for the GET (retrieve file) command
    public static final String COMMAND_GET = "GET";

    /// Constant for the PUT (upload file) command
    public static final String COMMAND_PUT = "PUT";

    /// Constant for the DELETE (delete file) command
    public static final String COMMAND_DELETE = "DELETE";

    /// Constant for the EXIT (shutdown) command
    public static final String COMMAND_EXIT = "EXIT";

    /// Constant for searching/operating on files by numeric ID
    public static final String SEARCH_BY_ID = "BY_ID";

    /// Constant for searching/operating on files by filename
    public static final String SEARCH_BY_NAME = "BY_NAME";
}
