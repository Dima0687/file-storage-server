package de.eisner.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/// ## FileService
///
/// Utility class for handling file operations on the client side.
/// Manages uploading files to the server and downloading files from the server.
///
/// **Design:** Static utility methods provide a clean interface for file I/O
/// with consistent error handling and logging.
public class FileService {
    /// Logger instance for the FileService class
    private static final Logger LOGGER = Logger.getLogger(FileService.class.getName());

    /// Default directory for client file operations
    private static final String DATA_DIR = "src/client/data";

    /// Private constructor to prevent instantiation of this utility class
    private FileService() {
    }

    /// Uploads a file from local storage to the server.
    ///
    /// **Process:**
    /// 1. Constructs the file path within the data directory
    /// 2. Verifies the file exists locally
    /// 3. Reads the complete file content into memory
    /// 4. Sends the file size followed by the raw bytes to the server
    /// 5. Logs successful upload with file size
    ///
    /// @param fileName The name of the file to upload
    /// @param output The output stream to write the file to
    /// @throws IOException If file reading or writing fails
    public static void uploadFile(String fileName, DataOutputStream output)
            throws IOException {
        Path filePath = Path.of(DATA_DIR, fileName);

        if (!Files.exists(filePath)) {
            System.out.println("File not found: " + filePath);
            return;
        }

        try {
            byte[] fileBytes = Files.readAllBytes(filePath);
            output.writeInt(fileBytes.length);
            output.write(fileBytes);
            output.flush();

            LOGGER.info("File uploaded: %s (\"%d\" bytes)".formatted(fileName, fileBytes.length));
        } catch (IOException e) {
            LOGGER.severe("Error uploading file: " + e.getMessage());
            throw e;
        }
    }

    /// Downloads a file from the server and saves it to local storage.
    ///
    /// **Process:**
    /// 1. Constructs the output file path within the data directory
    /// 2. Creates parent directories if they don't exist
    /// 3. Writes the file bytes using CREATE_NEW (prevents overwrites)
    /// 4. Logs successful download with file size
    ///
    /// @param fileName The desired local filename
    /// @param fileBytes The binary content of the file
    /// @throws IOException If directory creation or file writing fails
    public static void downloadFile(String fileName, byte[] fileBytes) throws IOException {
        Path outputPath = Path.of(DATA_DIR, fileName);

        try {
            Files.createDirectories(outputPath.getParent());

            Files.write(outputPath, fileBytes, StandardOpenOption.CREATE_NEW);
            LOGGER.info("File downloaded: %s (\"%d\" bytes)".formatted(fileName, fileBytes.length));
        } catch (IOException e) {
            LOGGER.severe("Error downloading file: " + e.getMessage());
            throw e;
        }
    }
}
