package com.myapp;

import java.io.BufferedReader; // Import the BufferedReader class for reading input
import java.io.BufferedWriter; // Import the BufferedWriter class for writing output
import java.io.IOException; // Import the IOException class for handling input/output errors
import java.io.InputStreamReader; // Import the InputStreamReader class for reading input
import java.io.OutputStreamWriter; // Import the OutputStreamWriter class for writing output
import java.net.Socket; // Import the Socket class for network communication
import java.util.concurrent.ExecutorService; // Import ExecutorService for managing a pool of threads
import java.util.concurrent.Executors; // Import Executors for creating ExecutorService instances
import java.util.function.Consumer;

public class Client {

    private final Socket socket; // Socket for communication with the server
    private final BufferedReader reader; // Read input from the server
    private final BufferedWriter writer; // Write output to the server
    private final String username; // Store the client's username
    private Consumer<String> onMessageReceived;

    public Client(String username, String host, int port) throws IOException {

        this.username = username;
        this.socket = new Socket(host, port);

        OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());
        InputStreamReader inputWriter = new InputStreamReader(socket.getInputStream());

        this.reader = new BufferedReader(inputWriter);
        this.writer = new BufferedWriter(outputWriter);
    
    }

    public void sendMessage(String message) throws IOException {
        // Send a message to the server
        writer.write(username + ": " + message + "\n");
        writer.flush(); // Flush the output to ensure the message is sent
    }

    public void listenForMessages() {
        try {
            String message;
            // Continuously listen for and print messages from the server
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            closeEverything(); // Handle errors and close resources
        }
    }

    // methods for GUI function !
    
    // Set up a callback function for message reception
    public void onMessageReceived(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    // Call this method to start listening for messages
    public void startMessageListener() throws IOException {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    if (onMessageReceived != null) {
                        onMessageReceived.accept(message);
                    }
                }
            } catch (IOException e) {
                closeEverything();
            }
        }).start();
    }

    public void closeEverything() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Close the socket if it is open
            }
            if (reader != null) {
                reader.close(); // Close the input reader
            }
            if (writer != null) {
                writer.close(); // Close the output writer
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle and log any errors
        }
    }

    public void connect() throws IOException {
        
        // Create a thread pool with two threads
        ExecutorService executorService = Executors.newFixedThreadPool(2); // Create a thread pool
        executorService.submit(this::listenForMessages); // Start a thread to listen for messages

        this.sendMessage("");
        
    }

}
