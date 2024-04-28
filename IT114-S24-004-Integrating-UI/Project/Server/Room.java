package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import Project.Common.Constants;

public class Room implements AutoCloseable {
    // server is a singleton now so we don't need this
    // protected static Server server;// used to refer to accessible server
    // functions
    private String name;
    private List<ServerThread> clients = new ArrayList<ServerThread>();
    private boolean isRunning = false;
    // Commands
    private final static String COMMAND_TRIGGER = "/";
    private final static String CREATE_ROOM = "createroom";
    private final static String JOIN_ROOM = "joinroom";
    private final static String DISCONNECT = "disconnect";
    private final static String LOGOUT = "logout";
    private final static String LOGOFF = "logoff";
    private final static String FLIP = "flip";
    private final static String ROLL = "roll";
    private final static String MUTE = "mute";
    private final static String UNMUTE = "unmute";
    private final static String EXPORT = "export";
    private static Logger logger = Logger.getLogger(Room.class.getName());
//    private List<String> mutedUsers = Collections.synchronizedList(new ArrayList<>());
    private List<MutedUser> mutedUsers = Collections.synchronizedList(new ArrayList<>());


    public Room(String name) {
        this.name = name;
        isRunning = true;
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return isRunning;
    }

    protected synchronized void addClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        client.setCurrentRoom(this);
        if (clients.indexOf(client) > -1) {
            logger.warning("Attempting to add client that already exists in room");
        } else {
            clients.add(client);
            client.sendResetUserList();
            syncCurrentUsers(client);
            sendConnectionStatus(client, true);
        }
    }

    protected synchronized void removeClient(ServerThread client) {
        if (!isRunning) {
            return;
        }
        // attempt to remove client from room
        try {
            clients.remove(client);
        } catch (Exception e) {
            logger.severe(String.format("Error removing client from room %s", e.getMessage()));
            e.printStackTrace();
        }
        // if there are still clients tell them this person left
        if (clients.size() > 0) {
            sendConnectionStatus(client, false);
        }
        checkClients();
    }

    private void syncCurrentUsers(ServerThread client) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread existingClient = iter.next();
            if (existingClient.getClientId() == client.getClientId()) {
                continue;// don't sync ourselves
            }
            boolean messageSent = client.sendExistingClient(existingClient.getClientId(),
                    existingClient.getClientName());
            if (!messageSent) {
                handleDisconnect(iter, existingClient);
                break;// since it's only 1 client receiving all the data, break if any 1 send fails
            }
        }
    }

    /***
     * Checks the number of clients.
     * If zero, begins the cleanup process to dispose of the room
     */
    private void checkClients() {
        // Cleanup if room is empty and not lobby
        if (!name.equalsIgnoreCase(Constants.LOBBY) && (clients == null || clients.size() == 0)) {
            close();
        }
    }

    /***
     * Helper function to process messages to trigger different functionality.
     * 
     * @param message The original message being sent
     * @param client  The sender of the message (since they'll be the ones
     *                triggering the actions)
     */
    @Deprecated // not used in my project as of this lesson, keeping it here in case things
                // change
    private boolean processCommands(String message, ServerThread client) {
        boolean wasCommand = false;
        try {
            if (message.startsWith(COMMAND_TRIGGER)) {
                String[] comm = message.split(COMMAND_TRIGGER);
                String part1 = comm[1];
                String[] comm2 = part1.split(" ");
                String command = comm2[0];
                String roomName;
                wasCommand = true;
                switch (command) {
                    case CREATE_ROOM:
                        roomName = comm2[1];
                        Room.createRoom(roomName, client);
                        break;
                    case JOIN_ROOM:
                        roomName = comm2[1];
                        Room.joinRoom(roomName, client);
                        break;
                    case DISCONNECT:
                    case LOGOUT:
                    case LOGOFF:
                        Room.disconnectClient(client, this);
                        break;
                    case FLIP:
                        int coin = (int)(Math.random()*2);
                        String value = "";
                        if (coin == 0){
                            value = "~tails~";
                        }
                        else if (coin == 1){
                            value = "~heads~";
                        }
                        sendMessage(client, String.format("~ %s flipped a coin and got %s~",client.getClientName(),"~"+value+"~"));
                    case ROLL:
                        if(comm2[1].contains("d")){
                            try{
                                String diceCount = message.split(" ")[1].split("d")[0];
                                String diceSides = message.split(" ")[1].split("d")[1];
                                int diceTotal = 0;
                                for (int i = 0; i < Integer.parseInt(diceCount); i++){
                                    int roll = (int)(Math.random()*Integer.parseInt(diceSides))+1;
                                    diceTotal += roll;
                                }
                                
                                sendMessage(client, String.format("<b> %s rolled %sd%s and got %s", client.getClientName(), diceCount, diceSides, diceTotal + "</b>"));
                            }catch(NumberFormatException e){
                                sendMessage(client, "~invalid input~");
                            }catch(ArrayIndexOutOfBoundsException e){
                                sendMessage(client, "~invalid input~");
                            }
                            break;
                        }else{
                            int result = (int)(Math.random()*Integer.parseInt(comm2[1]))+1;
                            sendMessage(client, "~Rolled a random number between 1 and ~" + "~"+comm2[1]+"~" + "~ and got: ~" + "~"+result+"~");
                            break;
                        }
                    case MUTE:
                        ServerThread mutedUser = findUserThreadInRoom(comm2[1]);
                        client.sendMuteUser(mutedUser, client);
                        break;

                    case UNMUTE:
                        ServerThread unMutedUser = findUserThreadInRoom(comm2[1]);
                        client.sendUnmuteUser(unMutedUser, client);
                        break;
                    case EXPORT:
                        client.exportMessages();
                        break;

                    default:
                        wasCommand = false;
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wasCommand;
    }

    //  Privatemessage method
        //UCID: jeo29
        //DATE: April 13, 2024
    private ServerThread findUser(String username){
        for (ServerThread user : clients) {
            if(user.getClientName().equals(username))
                return user;
        }
        return null;
    }

    // UCID: jeo29
    // Date: April 27, 2024
    private ServerThread findUserThreadInRoom(String username) {
        for (ServerThread client : clients) {
            if (client.getClientName().equals(username)) {
                return client;
            }
        }
        return null;
    }


    // Method to check if a user is already muted
    protected boolean isUserMuted(String muteduser,String mutingUser) {
        for (MutedUser mutedUser : mutedUsers) {
            if (mutedUser.getMutedUsername().equals(muteduser) && mutedUser.getMutingUser().equals(mutingUser) ) {
                return true;
            }
        }
        return false;
    }

    // UCID: jeo29
    // Date: April 27, 2024
    protected void muteUser(String usernameToMute, ServerThread mutingUser) {
        if (findUserThreadInRoom(usernameToMute) != null) {
            System.out.println("Muting user: " + usernameToMute);
            mutedUsers.add(new MutedUser(usernameToMute, mutingUser.getClientName()));
            // Notify the user that they have been muted by another user
            ServerThread userThread = findUserThreadInRoom(usernameToMute);
            if (userThread != null) {
                userThread.sendMessage(Constants.DEFAULT_CLIENT_ID, mutingUser.getClientName() + " muted you.");
            }
        } else {
            System.out.println("User " + usernameToMute + " not found or not in this room.");
        }
    }


    // UCID: jeo29
    // Date: April 27, 2024
    // Unmute a user
    protected void unmuteUser(String usernameToUnmute) {
        System.out.println("Unmuting user: " + usernameToUnmute);
        mutedUsers.removeIf(mutedUser -> mutedUser.getMutedUsername().equals(usernameToUnmute));

        // Notify the user that they have been unmuted
        ServerThread userThread = findUserThreadInRoom(usernameToUnmute);
        if (userThread != null) {
            userThread.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have been unmuted by another user.");
        }
    }

    // Command helper methods
    protected static void getRooms(String query, ServerThread client) {
        String[] rooms = Server.INSTANCE.getRooms(query).toArray(new String[0]);
        client.sendRoomsList(rooms,
                (rooms != null && rooms.length == 0) ? "No rooms found containing your query string" : null);
    }

    protected static void createRoom(String roomName, ServerThread client) {
        if (Server.INSTANCE.createNewRoom(roomName)) {
            Room.joinRoom(roomName, client);
        } else {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s already exists", roomName));
        }
    }

    /**
     * Will cause the client to leave the current room and be moved to the new room
     * if applicable
     * 
     * @param roomName
     * @param client
     */
    protected static void joinRoom(String roomName, ServerThread client) {
        if (!Server.INSTANCE.joinRoom(roomName, client)) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID, String.format("Room %s doesn't exist", roomName));
        }
    }

    protected static void disconnectClient(ServerThread client, Room room) {
        client.disconnect();
        room.removeClient(client);
    }
    // end command helper methods

    /***
     * Takes a sender and a message and broadcasts the message to all clients in
     * this room. Client is mostly passed for command purposes but we can also use
     * it to extract other client info.
     * 
     * @param sender  The client sending the message
     * @param message The message to broadcast inside the room
     */
    protected synchronized void sendMessage(ServerThread sender, String message) {
        if (!isRunning) {
            return;
        }
        logger.info(String.format("Sending message to %s clients", clients.size()));
        if (sender != null && processCommands(message, sender)) {
            // it was a command, don't broadcast
            return;
        }

//        // Check if the sender is muted, and if so, don't send the message
//        if (sender != null && mutedUsers.contains(sender.getClientName())) {
//            sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "You are muted and cannot send messages.");
//            return;
//        }

        //UCID: jeo29
        //Date: April 13, 2024
        if (message.contains("~")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("~")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<b>";
                    }
                    if (count == 2){
                        tEffects[i] = "</b>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "~";
            }
            for(String i: tEffects){
                message+= i;
            }
        }
    
        //UCID: jeo29
        //Date: April 13, 2024
        if (message.contains("!")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("!")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<i>";
                    }
                    if (count == 2){
                        tEffects[i] = "</i>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "!";
            }
            for(String i: tEffects){
                message+= i;
            }
        }
        
        //UCID: jeo29
        //Date: April 13, 2024

        if (message.contains("_")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("_")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<u>";
                    }
                    if (count == 2){
                        tEffects[i] = "</u>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "_";
            }
            for(String i: tEffects){
                message+= i;
            }
        }
        //UCID: jeo29
        //Date: April 13, 2024

        if (message.contains("%")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("%")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<font color=\"red\">";
                    }
                    if (count == 2){
                        tEffects[i] = "</font>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "%";
            }
            for(String i: tEffects){
                message+= i;
            }
        }
        
        //UCID: jeo29
        //Date: April 13, 2024

        if (message.contains("*")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("*")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<font color=\"green\">";
                    }
                    if (count == 2){
                        tEffects[i] = "</font>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "*";
            }
            for(String i: tEffects){
                message+= i;
            }
        }
        
        //UCID: jeo29
        //Date: April 13, 2024

        if (message.contains("^")){
            String[] tEffects = message.split("");
            message = "";
            int count = 0;
            int count2 = 0;
            int indexcount = 0;
            for (int i = 0; i < tEffects.length; i++){
                if (tEffects[i].equals("^")){
                    count++;
                    count2++;
                    if (count == 1){
                        indexcount = i;
                        tEffects[i] = "<font color=\"blue\">";
                    }
                    if (count == 2){
                        tEffects[i] = "</font>";
                        count = 0;
                    }
                }
            }
            if (count2%2 == 1){
                tEffects[indexcount] = "^";
            }
            for(String i: tEffects){
                message+= i;
            }
        }

        //UCID: jeo29
        //Date: April 13, 2024
        if (message.contains("@")){
            String[] words = message.split("\\s+");
            for(String word : words){
                if (word.startsWith("@")){
                    String privatemessageName = word.substring(1);
                    ServerThread targetUser = findUser(privatemessageName);

                    if (targetUser != null){
                        String senderMessage = "<font color=\"blue\">You privatemessage to " + targetUser.getClientName() + ":" + message.substring(privatemessageName.length()+1) + "</font>";
                        String receiverMessage = "<font color=\"blue\">privatemessage to you: " + message.substring(privatemessageName.length()+1) + "</font>";
                        targetUser.sendMessage(sender.getClientId(), receiverMessage);
                        sender.sendMessage(sender.getClientId(), senderMessage);
                        return;
                    } 
                }
            }
        }
        long from = sender == null ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            // UCID: jeo29
            // Date: April 27, 2024
            boolean shouldSendMessage = true;
            for (MutedUser mutedUser : mutedUsers) {
                System.out.println(mutedUser.getMutedUsername()+" "+mutedUser.getMutingUser());
                if (mutedUser.getMutedUsername().equals(sender.getClientName()) && mutedUser.getMutingUser().equals(client.getClientName())) {
                    System.out.println(sender.getClientName()+"this is sender if muted"+client.getClientName()+"this is client if not muted");
                    shouldSendMessage = false;
                    break;
                }
            }
            if (shouldSendMessage) {
            System.out.println(sender.getClientName()+"this is sender if not muted"+client.getClientName()+"this is client if not muted");
            boolean messageSent = client.sendMessage(from, message);
            if (!messageSent) {
                handleDisconnect(iter, client);
            }
            }
        }
    }

    protected synchronized void sendConnectionStatus(ServerThread sender, boolean isConnected) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread receivingClient = iter.next();
            boolean messageSent = receivingClient.sendConnectionStatus(
                    sender.getClientId(),
                    sender.getClientName(),
                    isConnected);
            if (!messageSent) {
                handleDisconnect(iter, receivingClient);
            }
        }
    }

    private void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
        iter.remove();
        logger.info(String.format("Removed client %s", client.getClientName()));
        sendMessage(null, client.getClientName() + " disconnected");
        checkClients();
    }

    public void close() {
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clients.clear();
    }
}