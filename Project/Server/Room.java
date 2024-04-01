package Server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Room {
    private String name;
    private Set<ServerThread> clients = Collections.synchronizedSet(new HashSet<>());

    public Room(String name) {
        this.name = name; // jeo29/March31,2024: Initialized room with name.
    }

    public synchronized void broadcastQuestion(String question, List<String> answers) {
        QuestionPayload payload = new QuestionPayload(question, answers); // jeo29/March31,2024: Constructing question payload.
        for (ServerThread client : clients) {
            client.sendPayload(payload); // jeo29/March31,2024: Broadcasting question payload to all clients.
        }
    }
    
    public String getName() {
        return name;
    }

    // Assume other methods like addClient, removeClient are here
}
