package Module4.Part3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private boolean isRunning = false;
    private ObjectOutputStream out; // Exposed here for send()
    private Server server; // Reference to our server so we can call methods on it more easily
    
    // Game state variables - jeo29 Feb 18, 2024
    private boolean numberGuessGameActive = false;
    private int secretNumber = 0;

    private void info(String message) {
        System.out.println(String.format("Thread[%s]: %s", getId(), message));
    }

    public ServerThread(Socket myClient, Server server) {
        info("Thread created");
        this.client = myClient;
        this.server = server;
    }

    public void disconnect() {
        info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    public boolean send(String message) {
        try {
            out.writeObject(message);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            cleanup();
            return false;
        }
    }

    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(client.getInputStream())) {
            this.out = out;
            isRunning = true;
            String fromClient;
            while (isRunning && (fromClient = (String) in.readObject()) != null) {
                info("Received from client: " + fromClient);
                
                // Handle the start of the number guess game (jeo29 Feb 18, 2024)
                if (fromClient.startsWith("/start")) {
                    numberGuessGameActive = true;
                    secretNumber = new Random().nextInt(100) + 1; // Assuming the range is 1-100
                    server.broadcast("Number guess game started. Try guessing a number between 1 and 100.", this.getId());
                } else if (fromClient.startsWith("/stop")) {
                    // Handle the stop of the number guess game (jeo29  Feb 18, 2024)
                    numberGuessGameActive = false;
                    server.broadcast("Number guess game has been stopped.", this.getId());
                } else if (fromClient.startsWith("/guess") && numberGuessGameActive) {
                    // Process guesses for the number guess game (jeo29 on Feb 18, 2024)
                    int guess = Integer.parseInt(fromClient.split(" ")[1]);
                    if (guess == secretNumber) {
                        server.broadcast("Correct guess! The number was " + secretNumber + ".", this.getId());
                        numberGuessGameActive = false; // Optionally reset the game
                    } else {
                        server.broadcast("Incorrect guess. Try again!", this.getId());
                    }
                } else if (fromClient.startsWith("/flip")) {
                    // Coin toss command handling (jeo29 Feb 18, 2024)
                    String result = new Random().nextBoolean() ? "heads" : "tails";
                    server.broadcast("Coin flip result: " + result, this.getId());
                } else {
                    // Forward non-command messages to all clients
                    server.broadcast(fromClient, this.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            info("Client disconnected");
        } finally {
            isRunning = false;
            cleanup();
        }
    }

    private void cleanup() {
        info("Thread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
    }
}
