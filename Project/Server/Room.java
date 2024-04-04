package Project.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import Project.common.Constants;

public class Room implements AutoCloseable {
    private String name;
    private List<ServerThread> clients = new ArrayList<ServerThread>();
    private boolean isRunning = false;
    private static Logger logger = Logger.getLogger(Room.class.getName());

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
            logger.warning("Attempting to add a client that already exists in the room");
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
        clients.remove(client);
        if (clients.size() > 0) {
            sendConnectionStatus(client, false);
        } else {
            close();
        }
    }

    private void syncCurrentUsers(ServerThread client) {
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread existingClient = iter.next();
            if (existingClient.getClientId() != client.getClientId()) {
                boolean messageSent = client.sendExistingClient(
                        existingClient.getClientId(),
                        existingClient.getClientName()
                );
                if (!messageSent) {
                    handleDisconnect(iter, existingClient);
                    break;
                }
            }
        }
    }

    private void handleDisconnect(Iterator<ServerThread> iter, ServerThread client) {
        iter.remove();
        logger.info(String.format("Removed client %s", client.getClientName()));
        sendMessage(null, client.getClientName() + " disconnected");
        if (clients.size() == 0) {
            close();
        }
    }

    public void close() {
        Server.INSTANCE.removeRoom(this);
        isRunning = false;
        clients.clear();
    }

    protected synchronized void sendMessage(ServerThread sender, String message) {
        if (!isRunning) {
            return;
        }
        logger.info(String.format("Sending message to %s clients", clients.size()));
        if (sender != null && processCommands(message, sender)) {
            return;
        }
        long from = sender == null ? Constants.DEFAULT_CLIENT_ID : sender.getClientId();
        Iterator<ServerThread> iter = clients.iterator();
        while (iter.hasNext()) {
            ServerThread client = iter.next();
            boolean messageSent = client.sendMessage(from, message);
            if (!messageSent) {
                handleDisconnect(iter, client);
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
                    isConnected
            );
            if (!messageSent) {
                handleDisconnect(iter, receivingClient);
            }
        }
    }
	//UCID:jeo29
    //Date:March 30,
	private boolean processCommands(String message, ServerThread client) {
		if (message.startsWith("/roll")) {
			String command = message.substring(6).trim();
			try {
				if (command.matches("\\d+d\\d+")) {
					// Format 2: /roll #d#
					String[] diceParts = command.split("d");
					int numberOfDice = Integer.parseInt(diceParts[0].trim());
					int numberOfSides = Integer.parseInt(diceParts[1].trim());
					StringBuilder result = new StringBuilder("Rolling " + numberOfDice + "d" + numberOfSides + ": ");
					for (int i = 0; i < numberOfDice; i++) {
						int roll = (int) (Math.random() * numberOfSides) + 1;
						result.append(roll);
						if (i < numberOfDice - 1) {
							result.append(", ");
						}
					}
					sendMessage(client, result.toString());
				} else {
					// Format 1: /roll 0 - X or 1 - X
					String[] rangeParts = command.split("-");
					int min = Integer.parseInt(rangeParts[0].trim());
					int max = Integer.parseInt(rangeParts[1].trim());
					int roll = (int) (Math.random() * (max - min + 1)) + min;
					sendMessage(client, "Rolling " + min + " - " + max + ": " + roll);
				}
				return true;
			} catch (Exception e) {
				return false;
			}
		} else if (message.equals("/flip")) {
			return handleFlipCommand(client);
		} else if (message.startsWith("/format")) {
			String formattedMessage = formatMessage(message.substring(8).trim());
			sendMessage(client, formattedMessage);
			return true;
		}
		return false;
	}
	
	//UCID:jeo29
    //Date:March 30,
    private boolean handleFlipCommand(ServerThread client) {
        int flip = (int) (Math.random() * 2);
        String result = (flip == 0) ? "Heads" : "Tails";
        sendMessage(client, "Coin flip: " + result);
        return true;
    }
///UCID:jeo29
//Date:March 30,
private String formatMessage(String message) {
    // Bold text
    message = message.replaceAll("\\*(.*?)\\*", "<b>$1</b>");
    // Italic text
    message = message.replaceAll("_(.*?)_", "<i>$1</i>");
    // Red text
    message = message.replaceAll("#r(.*?)#", "<span style=\"color:red\">$1</span>");
    // Green text
    message = message.replaceAll("#g(.*?)#", "<span style=\"color:green\">$1</span>");
    // Blue text
    message = message.replaceAll("#b(.*?)#", "<span style=\"color:blue\">$1</span>");
    // Underline text
    message = message.replaceAll("#u(.*?)#", "<u>$1</u>");
    return message;
}

	

    public void disconnectClient(ServerThread serverThread) {
    }

public static void main(String[] args) {
	try (Room room = new Room("Sample Room")) {
		String sourceMessage = "Hello *bold* _italic_ #rgreen# #bblue# #uunderline# text";
		String transformedMessage = room.formatMessage(sourceMessage);

		System.out.println("Source Message: " + sourceMessage);
		System.out.println("Transformed Message: " + transformedMessage);
	}
}
}
