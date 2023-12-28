package com.myapp;

import java.io.IOException; // import the IOException class for error handling.
import java.net.ServerSocket; // import ServerSocket for server initialization.
import java.net.Socket; // import Socket for client-server communication.

public class Server { // create a class named 'Server' for our server application.
    private ServerSocket serverSocket; // declare a ServerSocket instance for managing connections.
    public ClientHandler clientHandler;
    public Server(ServerSocket serverSocket) { // constructor for the 'Server' class.
        this.serverSocket = serverSocket; // assign the passed-in ServerSocket to the class instance.
    }

    public void startServer() { // method for starting the server.
        try {
            while (!serverSocket.isClosed()) { // run a loop while the server is open.
                   Socket socket = serverSocket.accept(); // accept incoming client connections.
                System.out.println("a new client has connected!"); // print a welcoming message.
                this.clientHandler = new ClientHandler(socket); // create a handler for the connected client.
                Thread thread = new Thread(this.clientHandler); // create a new thread for client handling.
                thread.start(); // start the client handling thread.
            }
        } catch (IOException e) { // catch any potential IOExceptions.
            e.printStackTrace(); // print the error message (improvement opportunity: log the error properly).
        }
    }

    public void close() { // method to close the server.
        try {
            if (serverSocket != null) { // check if the serverSocket exists.
                serverSocket.close(); // close the serverSocket.
            }
        } catch (IOException e) { // handle potential IOException.
            e.printStackTrace(); // show the error message.
        }
    }

    public static void main(String[] args) { // the main entry point of the server.
        try {
            ServerSocket serverSocket = new ServerSocket(1234); // create a server socket on port 1234.
            Server server = new Server(serverSocket); // create a server instance.
            server.startServer(); // start the server to listen for incoming connections.
        } catch (IOException e) { // handle potential IOException.
            e.printStackTrace(); // display the error message (log it better in a production application).
        }
    }
}

/**
 * The 'Server' class represents a simple server application for a chat service.
 * It imports necessary libraries for handling network connections and errors.
 *
 * The class includes methods to initialize, start, and close the server, as well as a main method
 * that serves as the entry point for the server application.
 *
 * In the constructor, a 'Server' instance is created, accepting a 'ServerSocket' to manage connections.
 * The 'startServer' method continuously listens for incoming client connections, creating a new
 * 'ClientHandler' for each connected client. Each client is processed in a separate thread for concurrent
 * communication.
 *
 * The 'close' method is used to gracefully close the server, closing the 'ServerSocket' and handling
 * any potential exceptions.
 *
 * The 'main' method is the entry point of the server application. It initializes a 'ServerSocket' on port 1234,
 * creates a 'Server' instance, and starts the server to listen for incoming client connections.
 * Any potential IOExceptions are handled and displayed in the console.
 */

