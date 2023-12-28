package com.myapp;

import java.io.BufferedReader; // Import the BufferedReader class for reading input
import java.io.BufferedWriter; // Import the BufferedWriter class for writing output
import java.io.IOException; // Import the IOException class for handling input/output errors
import java.io.InputStreamReader; // Import the InputStreamReader class for reading input
import java.io.OutputStreamWriter; // Import the OutputStreamWriter class for writing output
import java.net.Socket; // Import the Socket class for network communication
import java.util.ArrayList; // Import the ArrayList class for storing client handlers
import java.util.List; // Import the List interface for managing a list of client handlers

public class ClientHandler implements Runnable {
    // Declare a list to store all client handlers
    public static final List<ClientHandler> clientHandlers = new ArrayList<>();
    // Declare a socket to handle communication with a client
    private final Socket socket;
    // Declare a reader to read input from the client
    private final BufferedReader bufferedReader;
    // Declare a writer to send output to the client
    private final BufferedWriter bufferedWriter;
    // Store the client's username
    private final String clientUsername;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        // Initialize a reader to read input from the client
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // Initialize a writer to send output to the client
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        // Read the client's username
        String firstMessage = bufferedReader.readLine();
        this.clientUsername = firstMessage.split(":")[0];
        System.out.println("username logged as : " + clientUsername);
        // Add this client handler to the list of handlers
        clientHandlers.add(this);
        // Broadcast a message about the client's entry
        broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
    }

    @Override
    public void run() {
        String messageFromClient;
        try {
            // Continuously check for new messages from the client as long as the socket is open
            while (!socket.isClosed()) {
                // Read a message from the client
                messageFromClient = bufferedReader.readLine();
                // This should be starts with
                if (messageFromClient.contains("/msg")) {
                    
                    String [] splitMessage = messageFromClient.split(" ", 4);
                    String user = splitMessage[2];
                    String messageString = splitMessage[3];

                    selfMessage("Messaged "+user+" : "+messageString);
                    message(user, messageString);

                } else if (messageFromClient.contains("/list")) {

                    selfMessage("Users:");
                    for (ClientHandler clientHandler : clientHandlers) {
                        selfMessage(clientHandler.clientUsername);
                    }

                } else if (messageFromClient != null) {
                    // Broadcast the message to all clients
                    broadcastMessage(messageFromClient);
                }
            }
        } catch (IOException e) {
            // Handle errors and close resources
            closeEverything();
        }
    }

    public void selfMessage(String message) throws IOException {

        // Write the error message to the client
        this.bufferedWriter.write(message + "\n");
        // Flush the output to ensure the message is sent
        this.bufferedWriter.flush();

    }

    public void broadcastMessage(String messageToSend) {
        // Synchronize access to the list of client handlers
        synchronized (clientHandlers) {
            // Iterate through all client handlers in the list
            for (ClientHandler clientHandler : clientHandlers) {
                if (!clientHandler.equals(this)) {
                    try {
                        // Write the message to the output of another client handler
                        clientHandler.bufferedWriter.write(messageToSend + "\n");
                        // Flush the output to ensure the message is sent
                        clientHandler.bufferedWriter.flush();
                    } catch (IOException e) {
                        // Handle errors and close resources for the other client handler
                        clientHandler.closeEverything();
                    }
                }
            }
        }
    }

    public void message(String user, String messageToSend) {
        // Synchronize access to the list of client handlers
        synchronized (clientHandlers) {
            // Iterate through all client handlers in the list
            for (ClientHandler clientHandler : clientHandlers) {
                if (!clientHandler.equals(this)) {

                    if (clientHandler.clientUsername.equals(user)) {
                        try {
                            // Write the message to the output of another client handler
                            clientHandler.bufferedWriter.write("Private from "+this.clientUsername+" : "+messageToSend + "\n");
                            // Flush the output to ensure the message is sent
                            clientHandler.bufferedWriter.flush();
                        } catch (IOException e) {
                            // Handle errors and close resources for the other client handler
                            clientHandler.closeEverything();
                        }
                    }
                }
            }
        }
    }



    public void removeClientHandler() {
        // Synchronize access to the list of client handlers
        synchronized (clientHandlers) {
            // Remove this client handler from the list
            clientHandlers.remove(this);
        }
        // Broadcast a message about the client's departure
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything() {
        // Remove this client handler from the list and announce their departure
        removeClientHandler();
        try {
            // Close the client's socket
            socket.close();
            // Close the input reader
            bufferedReader.close();
            // Close the output writer
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace(); // Handle and log any errors
        }
    }

}

/**
 * The ClientHandler class represents a server-side client handler in a chat application.
 * It imports necessary libraries for network communication and handling client connections.
 *
 * ClientHandler maintains a list of active client handlers, each of which communicates with a client
 * over a network socket. It provides methods for sending and receiving messages, as well as managing clients.
 *
 * In the constructor, a new ClientHandler is created for each client's connection, with each instance
 * maintaining a unique client socket, input reader, and output writer. Upon connection, the client's username
 * is read, the ClientHandler is added to the list of active handlers, and a message is broadcasted
 * to notify other clients of the new arrival.
 *
 * The run method continuously listens for messages from the client and broadcasts them to other clients.
 * It operates as long as the client's socket remains open.
 *
 * The broadcastMessage method is used to send a message to all clients except the sender. It synchronizes
 * access to the list of client handlers to ensure thread safety.
 *
 * The removeClientHandler method removes a client handler from the list when the client disconnects
 * and broadcasts a departure message to other clients.
 *
 * The closeEverything method is called to handle resource cleanup. It closes the client's socket,
 * input reader, and output writer and handles any potential exceptions.
 */
