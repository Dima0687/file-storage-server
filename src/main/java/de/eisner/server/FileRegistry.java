
package de.eisner.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/// ## FileRegistry
///
/// Manages the persistent registry of uploaded files, maintaining a mapping between
/// unique file IDs and their corresponding filenames. This class ensures thread-safe
/// access to file metadata and persists the registry to disk for durability.
///
/// **Key Features:**
/// - Thread-safe concurrent access via ConcurrentHashMap
/// - Serialization support for registry persistence
/// - Automatic ID generation for uploaded files
/// - Cached size tracking for performance optimization
public class FileRegistry {
    /// Logger instance for the FileRegistry class
    private static final Logger LOGGER = Logger.getLogger(FileRegistry.class.getName());

    /// Concurrent map storing file ID to filename mappings
    private final Map<Long, String> idToFileName = new ConcurrentHashMap<>();

    /// Path to the persistent registry file on disk
    private final Path registryFile;

    /// Next ID to be assigned to a newly registered file
    private long nextId = 1;

    /// Cached size of the registry for performance optimization
    private volatile int cachedSize = 0;

    /// Constructs a new FileRegistry and loads existing registry data from disk.
    ///
    /// @param registryFile Path to the file that stores the registry data
    public FileRegistry(Path registryFile) {
        this.registryFile = registryFile;
        load();
    }

    /// Registers a new file in the registry and returns its unique ID.
    ///
    /// **Thread Safety:** This method is synchronized to ensure atomic registration
    /// and persistence of the new file.
    ///
    /// @param fileName The name of the file to register
    /// @return The unique ID assigned to the file
    public synchronized long registerFile(String fileName) {
        idToFileName.put(nextId, fileName);
        save();
        this.cachedSize = idToFileName.size();
        return nextId++;
    }

    /// Retrieves the filename associated with a given file ID.
    ///
    /// @param id The file ID to look up
    /// @return The filename, or null if the ID is not found
    public String getFileName(long id) {
        return idToFileName.get(id);
    }

    /// Removes a file from the registry by its ID and persists the change.
    ///
    /// **Thread Safety:** This method is synchronized to ensure atomic removal
    /// and persistence of the deletion.
    ///
    /// @param id The file ID to remove
    public synchronized void removeFile(long id) {
        String removed = idToFileName.remove(id);
        if (removed != null) {
            save();
            this.cachedSize = idToFileName.size();
        }
    }

    /// Saves the current registry state to disk using Java serialization.
    ///
    /// **Format:** Stores the idToFileName map and nextId as a serialized object stream.
    /// Creates parent directories if they don't exist.
    private void save() {
        try {
            Files.createDirectories(registryFile.getParent());

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(registryFile.toFile()))) {
                oos.writeObject(new HashMap<>(idToFileName));
                oos.writeLong(nextId);
                oos.flush();
            }
            LOGGER.info("FileRegistry saved successfully.");
        } catch (IOException e) {
            LOGGER.severe("Failed to save FileRegistry: " + e.getMessage());
        }
    }

    /// Loads the registry from disk and restores the file ID mappings.
    ///
    /// **Behavior:** If the registry file doesn't exist, it initializes with an empty map.
    /// In case of deserialization errors, it clears the map and resets the ID counter.
    @SuppressWarnings("unchecked")
    private void load() {
        try {
            if (!Files.exists(registryFile)) {
                LOGGER.info("FileRegistry file not found. Starting with empty registry.");
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(registryFile.toFile()))) {
                Map<Long, String> loaded = (Map<Long, String>) ois.readObject();
                idToFileName.putAll(loaded);
                nextId = ois.readInt();
            }
            LOGGER.info("FileRegistry loaded successfully. NextId = " + nextId);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe("Failed to load FileRegistry: " + e.getMessage());
            idToFileName.clear();
            nextId = 1;
        }
    }

    /// Returns the current number of files in the registry (cached value).
    ///
    /// @return The number of registered files
    public int size() {
        return cachedSize;
    }
}
