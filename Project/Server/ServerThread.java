package Server;

import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream out;

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket; // jeo29/March31,2024: Assigned client socket.
        initializeStreams();
    }

    private void initializeStreams() {
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream()); // jeo29/March31,2024: Setup output stream.
        } catch (Exception e) {
            e.printStackTrace(); // Error handling
        }
    }

    public void sendPayload(Payload payload) {
        try {
            out.writeObject(payload); // jeo29/March31,2024: Sending payload to client.
            out.flush();
        } catch (Exception e) {
            e.printStackTrace(); // Error handling
        }
    }

    @Override
    public void run() {
        // Handling incoming messages from client
    }

    // Additional methods for communication and client handling...
}
