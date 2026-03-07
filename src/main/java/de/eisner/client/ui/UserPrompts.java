package de.eisner.client.ui;

import java.util.Scanner;

import static de.eisner.client.protocol.ClientProtocol.SEARCH_BY_ID;
import static de.eisner.client.protocol.ClientProtocol.SEARCH_BY_NAME;

/// ## UserPrompts
///
/// Utility class for displaying prompts to the user and capturing their input.
/// Handles all user interface interactions on the client side.
///
/// **Responsibilities:**
/// - Display action menu and capture command selection
/// - Prompt for local and server filenames
/// - Prompt for file search criteria (by name or by ID)
/// - Prompt for download filename
public class UserPrompts {
    /// Scanner for reading user input from standard input
    private final Scanner scanner;

    /// Constructs a new UserPrompts instance.
    ///
    /// @param scanner The Scanner to use for reading user input
    public UserPrompts(Scanner scanner) {
        this.scanner = scanner;
    }

    /// Prompts the user to select an action from the main menu.
    ///
    /// **Options:**
    /// - 1: Get a file
    /// - 2: Create/upload a file
    /// - 3: Delete a file
    /// - **Hidden Option** - Exit
    ///
    /// @return The user's action selection
    public String promptAction() {
        System.out.print("Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ");
        return scanner.nextLine();
    }

    /// Prompts the user for the name of a local file to upload.
    ///
    /// @return The local filename
    public String promptLocalFileName() {
        System.out.print("Enter name of the file: ");
        return scanner.nextLine();
    }

    /// Prompts the user for the name the file should have on the server.
    ///
    /// **Behavior:** An empty response indicates the server should auto-generate a name.
    ///
    /// @return The desired server filename, or empty string for auto-naming
    public String promptServerFileName() {
        System.out.print("Enter name of the file to be saved on server (press Enter for auto-name): ");
        return scanner.nextLine();
    }

    /// Prompts the user for the filename to save a downloaded file as.
    ///
    /// @return The desired local filename
    public String promptDownloadFileName() {
        System.out.print("The file was downloaded! Specify a name for it: ");
        return scanner.nextLine();
    }

    /// Prompts the user to choose between searching by name or by ID.
    ///
    /// **Options:**
    /// - 1: Search by filename
    /// - 2: Search by file ID
    ///
    /// @param command The command being executed (GET, DELETE) for display
    /// @return A SearchChoice record containing search type and identifier
    public SearchChoice promptSearchType(String command) {
        System.out.printf("Do you want to %s the file by name or by id (1 - name, 2 - id): ", command.toLowerCase());
        String choice = scanner.nextLine();

        return switch (choice) {
            case "1" -> new SearchChoice(SEARCH_BY_NAME, promptIdentifier("filename"));
            case "2" -> new SearchChoice(SEARCH_BY_ID, promptIdentifier("id"));
            default -> {
                System.out.println("Invalid choice!");
                yield promptSearchType(command);
            }
        };
    }

    /// Prompts the user to enter an identifier (filename or ID).
    ///
    /// @param type The type of identifier being requested (for display)
    /// @return The user's input identifier
    private String promptIdentifier(String type) {
        System.out.print("Enter " + type + ": ");
        return scanner.nextLine();
    }

    /// A record representing the user's search choice.
    ///
    /// @param searchType The search type (by_name or by_id)
    /// @param identifier The search identifier (filename or ID)
    public record SearchChoice(String searchType, String identifier) {}
}
