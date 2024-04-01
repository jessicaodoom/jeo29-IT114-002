package Server; // Adjust as necessary for your package structure.

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientName;

    public Client(String host, int port, String clientName) {
        this.clientName = clientName;
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        listenForServerMessages();
    }

    private void listenForServerMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof QuestionPayload) {
                        QuestionPayload question = (QuestionPayload) obj;
                        System.out.println("Question: " + question.getQuestion()); // jeo29/March31,2024: Display question to player.
                        List<String> answers = question.getAnswers();
                        for (int i = 0; i < answers.size(); i++) {
                            System.out.println((i + 1) + ". " + answers.get(i)); // jeo29/March31,2024: Display answers to player.
                        }
                        // Code to capture player's answer and send back to server...
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Method to send payloads to server...
    // Main method to start client...
}
