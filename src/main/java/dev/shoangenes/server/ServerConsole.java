package dev.shoangenes.server;

import java.io.IOException;
import java.util.Scanner;

/**
 * ServerConsole class that provides a console interface for managing the FileServer.
 * It allows starting, stopping, and viewing the status of the server.
 * This class contains the main method and serves as the entry point for the application.
 */
public class ServerConsole {
    /*======================= Fields =======================*/

    private final Scanner scanner;

    /*======================= Constructors =======================*/

    private ServerConsole() {
        this.scanner = new Scanner(System.in);
    }

    /*======================= Static Factory Method =======================*/

    /**
     * Prints a decorative banner with the given title.
     * @param title The title to display in the banner
     */
    private static void printBanner(String title) {
        char topLeft = '╔';
        char topRight = '╗';
        char bottomLeft = '╚';
        char bottomRight = '╝';
        char horizontal = '═';
        char vertical = '║';

        int totalWidth = title.length() + 10;
        if (totalWidth < 30) totalWidth = 30;

        System.out.print(topLeft);
        for (int i = 0; i < totalWidth; i++) System.out.print(horizontal);
        System.out.println(topRight);

        int paddingTotal = totalWidth - title.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        System.out.print(vertical);
        for (int i = 0; i < paddingLeft; i++) System.out.print(' ');
        System.out.print(title);
        for (int i = 0; i < paddingRight; i++) System.out.print(' ');
        System.out.println(vertical);

        System.out.print(bottomLeft);
        for (int i = 0; i < totalWidth; i++) System.out.print(horizontal);
        System.out.println(bottomRight);
    }

    /**
     * Displays the menu and prompts the user for a choice.
     * Validates input to ensure it is an integer between 1 and 4.
     * @return The user's menu choice as an integer
     */
    private int menu() {
        System.out.println("1. Start Server");
        System.out.println("2. Stop Server");
        System.out.println("3. View Status");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
        int choice;
        while (true) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= 4) {
                    break;
                } else {
                    System.out.print("Invalid choice. Please enter a number between 1 and 4: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number between 1 and 4: ");
            }
        }
        return choice;
    }

    /**
     * Main method. Entry point for the File Server application.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        ServerConsole console = new ServerConsole();
        FileServer fileServer = null;
        printBanner("FILE SERVER v0");

        while (true) {
            switch (console.menu()) {
                case 1 -> {
                    if (fileServer == null || !fileServer.isRunning()) {
                        try {
                            fileServer = new FileServer();
                            fileServer.start();
                            System.out.println("Server started.");
                        } catch (IOException e) {
                            System.err.println("Failed to start server: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Server is already running.");
                    }
                }
                case 2 -> {
                    if (fileServer != null && fileServer.isRunning()) {
                        fileServer.stop();
                        System.out.println("Server stopped.");
                    } else {
                        System.out.println("Server is not running.");
                    }
                }
                case 3 -> {
                    if (fileServer == null) {
                        System.out.println("Server has not been initialized.");
                    } else {
                        int status = fileServer.getStatus();
                        String statusString = switch (status) {
                            case 0 -> "Stopped";
                            case 1 -> "Running";
                            case 2 -> "Terminated";
                            default -> "Unknown";
                        };
                        int activeConnections = fileServer.getActiveConnections();
                        System.out.println("Server Status: " + statusString);
                        System.out.println("Active Connections: " + activeConnections);
                    }
                }
                case 4 -> {
                    if (fileServer != null && fileServer.isRunning()) {
                        fileServer.stop();
                    }
                    System.out.println("Exiting...");
                    return;
                }
            }
        }
    }
}
