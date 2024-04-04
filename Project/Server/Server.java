package Project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import Project.common.Constants;

public enum Server {
    INSTANCE;

    private int port = 3001;
    private static Logger logger = Logger.getLogger(Server.class.getName());
    private List<Room> rooms = new ArrayList<>();
    private Room lobby = null; // default room
    private long nextClientId = 1;

    private Queue<ServerThread> incomingClients = new LinkedList<>();
    private volatile boolean isRunning = false;

    private void start(int port) {
        this.port = port;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket incomingClient = null;
            logger.info(String.format("Server is listening on port %s", port));
            isRunning = true;
            startQueueManager();
            lobby = new Room(Constants.LOBBY);
            rooms.add(lobby);
            do {
                logger.info("Waiting for next client");
                if (incomingClient != null) {
                    logger.info("Client connected");
                    ServerThread sClient = new ServerThread(incomingClient, lobby);
                    sClient.start();
                    incomingClients.add(sClient);
                    incomingClient = null;
                }
            } while ((incomingClient = serverSocket.accept()) != null);
        } catch (IOException e) {
            logger.severe("Error accepting connection");
            e.printStackTrace();
        } finally {
            logger.info("Closing Server Socket");
        }
    }

    private void startQueueManager() {
        new Thread() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (incomingClients.size() > 0) {
                        ServerThread ic = incomingClients.peek();
                        if (ic != null) {
                            if (ic.isRunning() && ic.getClientName() != null) {
                                handleIncomingClient(ic);
                                incomingClients.poll();
                            }
                        }
                    }
                }
            }
        }.start();
    }

    private void handleIncomingClient(ServerThread client) {
        client.setClientId(nextClientId);
        client.sendClientId(nextClientId);
        nextClientId++;
        if (nextClientId < 0) {
            nextClientId = 1;
        }
        joinRoom(Constants.LOBBY, client);
    }

    private Room getRoom(String roomName) {
        for (Room room : rooms) {
            if (room.getName().equalsIgnoreCase(roomName)) {
                return room;
            }
        }
        return null;
    }

    protected synchronized boolean joinRoom(String roomName, ServerThread client) {
        Room newRoom = roomName.equalsIgnoreCase(Constants.LOBBY) ? lobby : getRoom(roomName);
        Room oldRoom = client.getCurrentRoom();
        if (newRoom != null && roomName != null) {
            if (oldRoom != null && oldRoom != newRoom) {
                logger.info(String.format("Client %s leaving old room %s", client.getClientName(),
                        oldRoom.getName()));
                oldRoom.removeClient(client);
            }
            logger.info(String.format("Client %s joining new room %s", client.getClientName(), newRoom.getName()));
            newRoom.addClient(client);
            return true;
        }
        return false;
    }

    protected synchronized boolean createNewRoom(String roomName, ServerThread client) {
        if (getRoom(roomName) != null) {
            logger.info(String.format("Room %s already exists", roomName));
            return false;
        } else {
            Room newRoom = new Room(roomName);
            rooms.add(newRoom);
            logger.info(String.format("Created new room %s", roomName));
            joinRoom(roomName, client);
            return true;
        }
    }

    protected synchronized List<String> getRooms(String query) {
        return getRooms(query, 10);
    }

    protected synchronized List<String> getRooms(String query, int limit) {
        List<String> matchedRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isRunning() && room.getName().toLowerCase().contains(query.toLowerCase())) {
                matchedRooms.add(room.getName());
                if (matchedRooms.size() >= limit) {
                    break;
                }
            }
        }
        return matchedRooms;
    }

    protected synchronized void removeRoom(Room r) {
        if (rooms.remove(r)) {
            logger.info(String.format("Removed empty room %s", r.getName()));
        }
    }

    protected synchronized void broadcast(String message) {
        if (processCommand(message)) {
            return;
        }
        for (Room room : rooms) {
            if (room != null) {
                room.sendMessage(null, message);
            }
        }
    }

    private boolean processCommand(String message) {
        System.out.println("Checking command: " + message);
        // TODO: Implement your custom command logic here
        return false;
    }

    public static void main(String[] args) {
        Server.logger.info("Starting server");
        int port = Server.INSTANCE.port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        Server.INSTANCE.start(port);
        Server.logger.info("Server stopped");
    }

    public void getRooms(String trim, ServerThread serverThread) {
    }
}
