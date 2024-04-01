package Server;

import java.util.*;
import java.util.concurrent.*;

import Common.TriviaDataLoader;
import Common.TriviaQuestion;

public class GameRoom extends Room {
    private Map<String, Integer> playerAnswers = new ConcurrentHashMap<>(); // Tracks player's answer index.
    private Map<String, Long> playerAnswerTimes = new ConcurrentHashMap<>(); // Tracks the order of answers by timestamp.
    private ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private TriviaDataLoader dataLoader = new TriviaDataLoader();
    private int roundDurationSeconds = 30; // Duration of each round in seconds.
    private int winningScore = 5; // Example winning score.

    public GameRoom(String name) {
        super(name); // jeo29/March31,2024: Room name initialization.
    }

    public void startTriviaRound() {
        resetPlayerAnswers(); // jeo29/March31,2024: Clear previous round answers.
        String category = dataLoader.pickRandomCategory(); // jeo29/March31,2024: Pick a random category.
        TriviaQuestion question = dataLoader.pickRandomQuestion(category); // jeo29/March31,2024: Pick a random question within the category.

        QuestionPayload payload = new QuestionPayload(question.getQuestionText(), question.getOptions());
        broadcastPayload(payload); // jeo29/March31,2024: Broadcasting question payload to clients.
        
        // Start round timer
        startRoundTimer(); // jeo29/March31,2024: Starting round timer.
    }

    private void resetPlayerAnswers() {
        playerAnswers.clear(); // jeo29/March31,2024: Resetting player answers for new round.
        playerAnswerTimes.clear(); // jeo29/March31,2024: Resetting answer times for new round.
    }

    private void startRoundTimer() {
        timer.schedule(this::endRound, roundDurationSeconds, TimeUnit.SECONDS); // jeo29/March31,2024: Scheduling end of round.
    }

    public synchronized void recordPlayerAnswer(String playerID, int answerIndex) {
        playerAnswers.put(playerID, answerIndex); // jeo29/March31,2024: Record player's choice.
        playerAnswerTimes.put(playerID, System.currentTimeMillis()); // jeo29/March31,2024: Record answer time for ordering.
        // You might also handle changing answers here
    }

    private void endRound() {
        // Scoring and preparing for the next question
        // jeo29/March31,2024: Check if any player has won.
        
        checkForWinners(); // jeo29/March31,2024: Checking for any winners based on score.
    }

    private void checkForWinners() {
        //Tracking if there is a winner
        // jeo29/March31,2024:Check the game and provide scores
        
        Map<String, Integer> scores = new HashMap<>(); // Tracking after each round based on correct answers.
        boolean gameEnded = scores.entrySet().stream().anyMatch(entry -> entry.getValue() >= winningScore);
        
        if (gameEnded) {
            // End the game session, handle ties, etc.
            // jeo29/March31,2024: Handling game end and notifying players.
            timer.shutdownNow(); // jeo29/March31,2024: Shutting down round timer as game ends.
        } else {
            // If no winner, start the next round.
            startTriviaRound(); // jeo29/March31,2024: Starting new round if no winner.
        }
    }
    
   
}
