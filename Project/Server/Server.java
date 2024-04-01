package Server; // jeo29/March 31,2024: Corrected package declaration.

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port = 3000; // jeo29/March 31,2024: Default port for server.
    private List<Room> rooms = new ArrayList<>(); // jeo29/March 31,2024: List of rooms.
    private Room lobby; // jeo29/March 31,2024: Lobby room where clients first join.

    public Server(int port) {
        this.port = port;
        lobby = new Room("Lobby"); // jeo29/March 31,2024: Create a lobby on server start.
        rooms.add(lobby); // jeo29/March 31,2024: Add lobby to rooms list.
    }

    public void start() {
        System.out.println("Server starting on port " + port); // jeo29/March 31,2024: Starting server message.

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Listening for connections..."); // jeo29/March 31,2024: Server started message.
            
            while (true) {
                Socket clientSocket = serverSocket.accept(); // jeo29/March 31,2024: Accept new client connections.
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress()); // jeo29/March 31,2024: Client connected message.

                ServerThread thread = new ServerThread(clientSocket, lobby); // jeo29/March 31,2024: Create a new server thread for each client.
                thread.start(); // jeo29/March 31,2024: Start the server thread.

                lobby.addClient(thread); // jeo29/March 31,2024: Add new client to the lobby.
            }
        } catch (IOException e) {
            e.printStackTrace(); // jeo29/March 31,2024: Exception handling.
        }
    }

    protected synchronized boolean createNewRoom(String roomName) {
        if (getRoom(roomName) == null) {
            Room room = new Room(roomName); // jeo29/March 31,2024: New room creation.
            rooms.add(room); // jeo29/March 31,2024: Add new room to list of rooms.
            System.out.println("New room created: " + roomName); // jeo29/March 31,2024: Log new room creation.
            return true;
        } else {
            System.out.println("Room already exists: " + roomName); // jeo29/March 31,2024: Log room existence.
            return false;
        }
    }

    private Room getRoom(String roomName) {
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(roomName)) {
                return room; // jeo29/March 31,2024: Return the room if it exists.
            }
        }
        return null; // jeo29/March 31,2024: If room does not exist, return null.
    }

    // ... Rest of the Server class
}

