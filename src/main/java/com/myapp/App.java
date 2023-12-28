package com.myapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.concurrent.TimeUnit; 

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    private Client chatClient;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private TextArea chatArea;
    private TextField messageField;
    private VBox sidebar;
    private String host;
    private int port;
    private boolean isHost;

    private void showStartupDialog(Stage primaryStage) {
        // Create the custom dialog.
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Server Configuration");

        // Set the button types.
        ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        // Create the host and port fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField hostField = new TextField("localhost");
        hostField.setPromptText("Host");
        TextField portField = new TextField("1234");
        portField.setPromptText("Port");
        CheckBox isHostCheckBox = new CheckBox("Host Server");
        isHostCheckBox.setSelected(true);

        grid.add(new Label("Host:"), 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(portField, 1, 1);
        grid.add(isHostCheckBox, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the host field by default.
        Platform.runLater(hostField::requestFocus);

        // Convert the result to a host-port-pair when the connect button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                return new Pair<>(hostField.getText(), Integer.parseInt(portField.getText()));
            }
            return null;
        });

        Optional<Pair<String, Integer>> result = dialog.showAndWait();

        result.ifPresent(hostPort -> {
            this.host = hostPort.getKey();
            this.port = hostPort.getValue();
            this.isHost = isHostCheckBox.isSelected();

            if (this.isHost) {
                startServer(this.port);
            }

            // Proceed to show the main UI.
            setupPrimaryStage(primaryStage);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        showStartupDialog(primaryStage);
    }

    public void setupPrimaryStage(Stage primaryStage) {

        // Chat area setup
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setMouseTransparent(true);

        // Input field setup
        messageField = new TextField();
        messageField.setPromptText("Enter your message here");

        // Send button setup
        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> sendMessage(messageField.getText()));

        // Horizontal box layout for the message field and send button
        HBox inputLayout = new HBox(5, messageField, sendButton);
        inputLayout.setAlignment(Pos.CENTER_LEFT);

        // Main chat layout
        VBox chatLayout = new VBox(10, chatArea, inputLayout);
        chatLayout.setAlignment(Pos.CENTER_LEFT);
        chatLayout.setPadding(new Insets(10));

        // Sidebar setup
        sidebar = new VBox(10);
        sidebar.setPadding(new Insets(5));

        Label sidebarLabel = new Label("Commands:");
        Label sidebarLabel2 = new Label("/msg <username> <message> \nSend a private message to that user");
        Label sidebarLabel3 = new Label("/list \nList all users in the chat");

        sidebar.getChildren().add(sidebarLabel);
        sidebar.getChildren().add(sidebarLabel2);
        sidebar.getChildren().add(sidebarLabel3);

        // Root layout containing both chatLayout and sidebar
        HBox rootLayout = new HBox(10, chatLayout, sidebar);

        // Scene setup
        Scene scene = new Scene(rootLayout, 800, 300); // Adjusted width

        // Stage setup
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Prompt for username and connect to the server !
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chat Client");
        dialog.setHeaderText("Enter your username");
        dialog.setContentText("Username:");
        Optional<String> username = dialog.showAndWait();
        username.ifPresentOrElse(
            this::connectToServer, 
            () -> showErrorAndExit("A username is required to connect to the server.")
        );
        
    }

    private void connectToServer(String username) {
        try {
            if (username.length() < 1) {
                showErrorAndExit("You need to enter a username.");
            }
            chatClient = new Client(username, this.host, this.port);
    
            chatClient.onMessageReceived(message -> {
                Platform.runLater(() -> {
                    chatArea.appendText(message + "\n");
                });
            });
            chatClient.startMessageListener();
        } catch (IOException e) {
            showErrorAndExit("Could not connect to the server. Please try again later.");
        }

    }
    
    private void showErrorAndExit(String errorMessage) {
        // Alert must be created and shown in the JavaFX Application thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
    
            // Show the alert and wait for it to be closed before exiting the application
            alert.showAndWait();
            Platform.exit();
        });
    }

    private void sendMessage(String message) {
        if (chatClient != null && !message.isEmpty()) {
            try {
                chatClient.sendMessage(message); // Send the message through the Client instance
                final String formattedMessage = "Me: " + message + "\n"; // Format the message to indicate it's from the current user
                Platform.runLater(() -> {
                    chatArea.appendText(formattedMessage); // Append the message to the chat area
                });
                messageField.clear(); // Clear the input field for new messages
            } catch (IOException e) {
                // Handle exception (e.g., show error dialog to the user)
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void stop() {
        if (chatClient != null) {
            chatClient.closeEverything();
        }
        executorService.shutdownNow();
    }
    
    public void startServer(int port) {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port); // Use a try-with-resources or close it later
                Server server = new Server(serverSocket);
                server.startServer(); // This method must not block; it should start a new thread for each client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        
    }

    public static void main(String[] args) throws InterruptedException {
        launch(args); // Launch the JavaFX application
    }
}