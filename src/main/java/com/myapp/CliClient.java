package com.myapp;

import java.io.IOException; // Import the IOException class for handling input/output errors
import java.util.Scanner; // Import the Scanner class for user input

public class CliClient {


    public CliClient() {

    }


    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username for the group chat: ");
            String username = scanner.nextLine();

            Client client = new Client(username, "localhost", 1234);
            client.connect(); // Connect to the server

            while (true) {
                System.out.print("Enter a message: ");
                String message = scanner.nextLine();
                if (!message.isEmpty()) {
                    client.sendMessage(message); // Send the user's message to the server
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle and log any errors
        }
    }
}
/**
 * This is a Java client application for a group chat.
 * It establishes a connection to a chat server, allowing users to send and receive messages.
 * The client class imports necessary libraries for network communication, user input, and thread management.
 *
 * The Client class includes methods for sending a username, sending messages, listening for server messages,
 * and closing resources in case of an error or when exiting.
 *
 * The main method initiates the client application. It prompts the user to enter a username,
 * connects to the chat server via a socket, and initializes a Client instance.
 * The client sends the username to the server and sets up a thread pool for listening to incoming messages.
 * It continually prompts the user to input messages, which are sent to the server.
 *
 * In case of an error or when the client exits, the application closes the connection and resources.
 */
