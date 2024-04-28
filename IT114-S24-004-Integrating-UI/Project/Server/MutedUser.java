package Project.Server;


public class MutedUser {
    private String mutedUsername;
    private String mutingUser;

    public MutedUser(String mutedUsername, String mutingUser) {
        this.mutedUsername = mutedUsername;
        this.mutingUser = mutingUser;
    }

    public String getMutedUsername() {
        return mutedUsername;
    }

    public String getMutingUser() {
        return mutingUser;
    }
}