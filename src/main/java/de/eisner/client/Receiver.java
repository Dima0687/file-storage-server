package de.eisner.client;

import de.eisner.client.protocol.ClientResponseCode;
import de.eisner.client.ui.UserPrompts;

import java.io.DataInput;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

/// ## Receiver
///
/// Represents a client-side thread responsible for receiving and processing
/// responses from the server. Handles response parsing, message display, and
/// file download initiation for GET operations.
///
/// **Lifecycle:**
/// 1. Reads a UTF response from the server
/// 2. Parses the response code and optional data
/// 3. Displays appropriate message to the user
/// 4. Initiates file download if it's a successful GET response
public class Receiver extends Thread {
    /// Logger instance for the Receiver class
    private static final Logger LOGGER = Logger.getLogger(Receiver.class.getName());

    /// Input stream for reading server responses
    private final DataInput input;

    /// The command that was sent to the server
    private final String actionSend;

    /// Constructs a new Receiver for the specified action.
    ///
    /// @param input The input stream to read responses from
    /// @param actionSend The command that was sent (GET, PUT, DELETE, EXIT)
    public Receiver(DataInput input, String actionSend) {
        this.input = input;
        this.actionSend = actionSend;
    }

    /// Main thread execution method.
    ///
    /// **Process:**
    /// 1. Reads UTF response from server
    /// 2. Parses response code and optional data
    /// 3. Retrieves appropriate message via ClientResponseCode
    /// 4. Displays message to user
    /// 5. Initiates file download for successful GET responses
    @Override
    public void run() {
        try {
            String rawResponse = input.readUTF();
            String[] response = rawResponse.split(" ", 2);

            if (response.length < 1) {
                LOGGER.warning("Corrupted response!");
                return;
            }

            int code = Integer.parseInt(response[0]);
            String content = response.length == 2 ? response[1] : null;

            String message = ClientResponseCode.getMessage(actionSend, code, content);
            System.out.println(message);

            if ("GET".equals(actionSend) && code == 200) {
                downloadFile();
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Wrong code in response: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.warning("Client disconnected: " + e.getMessage());
        }
    }

    /// Initiates file download by reading file bytes and prompting for save location.
    ///
    /// **Process:**
    /// 1. Reads file length from input stream
    /// 2. Validates that length is non-negative
    /// 3. Reads complete file content
    /// 4. Prompts user for desired save location
    /// 5. Delegates to FileService for actual file writing
    ///
    /// @throws IOException If reading from the input stream fails
    private void downloadFile() throws IOException {
        int fileLength = input.readInt();
        if (fileLength < 0) {
            System.out.println("Error: Invalid file length");
            return;
        }

        byte[] fileBytes = new byte[fileLength];
        input.readFully(fileBytes, 0, fileLength);

        UserPrompts prompts = new UserPrompts(new Scanner(System.in));
        String localFileName = prompts.promptDownloadFileName();

        FileService.downloadFile(localFileName, fileBytes);
        System.out.println("File saved on the hard drive!");
    }
}
