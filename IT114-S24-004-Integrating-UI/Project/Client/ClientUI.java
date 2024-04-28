package Project.Client;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Arrays;


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import Project.Client.Views.ChatPanel;
import Project.Client.Views.ConnectionPanel;
import Project.Client.Views.Menu;
import Project.Client.Views.RoomsPanel;
import Project.Client.Views.UserInputPanel;
import Project.Common.Constants;
import Project.Server.MutedUser;

public class ClientUI extends JFrame implements IClientEvents, ICardControls {
    CardLayout card = null;// accessible so we can call next() and previous()
    Container container;// accessible to be passed to card methods
    String originalTitle = null;
    private static Logger logger = Logger.getLogger(ClientUI.class.getName());
    private JPanel currentCardPanel = null;
    private Card currentCard = Card.CONNECT;

    private Hashtable<Long, String> userList = new Hashtable<Long, String>();

    private long myId = Constants.DEFAULT_CLIENT_ID;
    private JMenuBar menu;
    // Panels
    private ConnectionPanel csPanel;
    private UserInputPanel inputPanel;
    private RoomsPanel roomsPanel;
    private ChatPanel chatPanel;

    public ClientUI(String title) {
        super(title);// call the parent's constructor
        originalTitle = title;
        container = getContentPane();
        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // System.out.println("Resized to " + e.getComponent().getSize());
                // rough concepts for handling resize
                container.setPreferredSize(e.getComponent().getSize());
                container.revalidate();
                container.repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
        
        setMinimumSize(new Dimension(800, 300));
        // centers window
        setLocationRelativeTo(null);
        card = new CardLayout();
        setLayout(card);
        // menu
        menu = new Menu(this);
        this.setJMenuBar(menu);
        // separate views
        csPanel = new ConnectionPanel(this);
        inputPanel = new UserInputPanel(this);
        chatPanel = new ChatPanel(this);
        
        roomsPanel = new RoomsPanel(this);


        // https://stackoverflow.com/a/9093526
        // this tells the x button what to do (updated to be controlled via a prompt)
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int response = JOptionPane.showConfirmDialog(container,
                "Are you sure you want to close this window?", "Close Window?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        Client.INSTANCE.sendDisconnect();
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });
        // lastly
        pack();// tells the window to resize itself and do the layout management
        setVisible(true);
    }
    void findAndSetCurrentPanel(){
        for (Component c : container.getComponents()) {
            if (c.isVisible()) {
                currentCardPanel = (JPanel) c;
                currentCard = Enum.valueOf(Card.class, currentCardPanel.getName());
                //if we're not connected don't access anything that requires a connection
                if(myId == Constants.DEFAULT_CLIENT_ID && currentCard.ordinal() >= Card.CHAT.ordinal()){
                    show(Card.CONNECT.name());
                }
                break;
            }
        }
        System.out.println(currentCardPanel.getName());
    }
    @Override
    public void next() {
        card.next(container);
        findAndSetCurrentPanel();
    }

    @Override
    public void previous() {
        card.previous(container);
        
    }

    @Override
    public void show(String cardName) {
        card.show(container, cardName);
        findAndSetCurrentPanel();
    }

    @Override
    public void addPanel(String cardName, JPanel panel) {
        this.add(cardName, panel);
    }

    @Override
    public void connect() {
        String username = inputPanel.getUsername();
        String host = csPanel.getHost();
        int port = csPanel.getPort();
        setTitle(originalTitle + " - " + username);
        Client.INSTANCE.connect(host, port, username, this);
        //TODO add connecting screen/notice
    }

    public static void main(String[] args) {
        new ClientUI("Client");
    }

    private String mapClientId(long clientId) {
        String clientName = userList.get(clientId);
        if (clientName == null) {
            clientName = "Server";
        }
        return clientName;
    }

    /**
     * Used to handle new client connects/disconnects or existing client lists (one
     * by one)
     * 
     * @param clientId
     * @param clientName
     * @param isConnect
     */
    private synchronized void processClientConnectionStatus(long clientId, String clientName, boolean isConnect) {
        if (isConnect) {
            if (!userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Adding %s[%s]", clientName, clientId));
                userList.put(clientId, clientName);
                chatPanel.addUserListItem(clientId, String.format("%s (%s)", clientName, clientId));
            }
        } else {
            if (userList.containsKey(clientId)) {
                logger.log(Level.INFO, String.format("Removing %s[%s]", clientName, clientId));
                userList.remove(clientId);
                chatPanel.removeUserListItem(clientId);
            }
            if (clientId == myId) {
                logger.log(Level.INFO, "I disconnected");
                myId = Constants.DEFAULT_CLIENT_ID;
                previous();
            }
        }
    }

    @Override
    public void onClientConnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onClientDisconnect(long clientId, String clientName, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, false);
            chatPanel.addText(String.format("*%s %s*", clientName, message));
        }
    }

    @Override
    public void onMessageReceive(long clientId, String message) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            String clientName = mapClientId(clientId);
            chatPanel.addText(String.format("%s: %s", clientName, message));
        }
    }



    // UCID: jeo29
    // Date: April 27, 2024
    public void exportMessage() {
        try {
            List<String> messages = Arrays.asList(chatPanel.getAllChatMessages());

            // Create a JFileChooser instance
            JFileChooser fileChooser = new JFileChooser();

            // Set the file filter to accept only .txt files
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
            fileChooser.setFileFilter(filter);

            // Show the save dialog and get the user's choice
            int userChoice = fileChooser.showSaveDialog(null);

            // If the user chooses to save the file
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File selectedFile = fileChooser.getSelectedFile();

                // Create a FileWriter with the selected file
                FileWriter fileWriter = new FileWriter(selectedFile);
                // Create a BufferedWriter to efficiently write characters to the file
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                // Iterate through each message
                for (String mes : messages) {
                    // Convert HTML message to plain text (You need to implement this)
                    String plainTextMessage = mes;
                    // Write the plain text message to the file
                    bufferedWriter.write(plainTextMessage);
                    bufferedWriter.newLine(); // Add a new line after each message
                }
                // Close the BufferedWriter
                bufferedWriter.close();

                System.out.println("Messages exported successfully to: " + selectedFile.getAbsolutePath());
            } else {
                System.out.println("Export operation canceled by the user.");
            }
        } catch (IOException e) {
            // Handle the IOException
            e.printStackTrace();
        }
    }


    // UCID: jeo29
    // Date: April 27, 2024
    @Override
    public void onMuteUser(long clientId,String clientName)
    {
        chatPanel.updateUserListItem(clientId,clientName);
    }

    @Override
    public void onUnMuteUser(long clientId,String clientName){
        chatPanel.updateUserListItem(clientId,clientName);
    }

    @Override
    public void onReceiveClientId(long id) {
        if (myId == Constants.DEFAULT_CLIENT_ID) {
            myId = id;
            show(Card.CHAT.name());
        } else {
            logger.log(Level.WARNING, "Received client id after already being set, this shouldn't happen");
        }
    }

    @Override
    public void onResetUserList() {
        userList.clear();
        chatPanel.clearUserList();
    }

    @Override
    public void onSyncClient(long clientId, String clientName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            processClientConnectionStatus(clientId, clientName, true);
        }
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
        roomsPanel.removeAllRooms();
        if (message != null && message.length() > 0) {
            roomsPanel.setMessage(message);
        }
        if (rooms != null) {
            for (String room : rooms) {
                roomsPanel.addRoom(room);
            }
        }
    }

    @Override
    public void onRoomJoin(String roomName) {
        if (currentCard.ordinal() >= Card.CHAT.ordinal()) {
            chatPanel.addText("Joined room " + roomName);
        }
    }


    // UCID: jeo29
    // April 27, 2024
    //method for highlight feature
    @Override
    public void recentUser(long clientId){
        chatPanel.recentUser(clientId);
    }
    @Override
    public void chatH() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'chatH'");
    }
}
