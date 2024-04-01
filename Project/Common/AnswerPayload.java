package Server; // jeo29/March31,2024: Package declaration.

public class AnswerPayload extends Payload { // jeo29/March31,2024: Answer payload for trivia.
    private String playerID;
    private int answerIndex; // jeo29/March31,2024: Index of the selected answer.

    public AnswerPayload(String playerID, int answerIndex) { // jeo29/March31,2024: Constructor.
        super(PayloadType.ANSWER);
        this.playerID = playerID;
        this.answerIndex = answerIndex;
    }

    public String getPlayerID() { // jeo29/March31,2024: Get player ID.
        return playerID;
    }

    public int getAnswerIndex() { // jeo29/March31,2024: Get answer index.
        return answerIndex;
    }
}
