package Server; // jeo29/March31,2024: Package declaration.

public class ReadyPayload extends Payload { // jeo29/March31,2024: Ready payload for players.
    private String playerID;

    public ReadyPayload(String playerID) { // jeo29/March31,2024: Constructor.
        super(PayloadType.READY);
        this.playerID = playerID;
    }

    public String getPlayerID() { // jeo29/March31,2024: Get player ID.
        return playerID;
    }
}
