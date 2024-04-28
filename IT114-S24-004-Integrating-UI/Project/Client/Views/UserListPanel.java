package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import Project.Client.ClientUtils;
import Project.Client.ICardControls;

public class UserListPanel extends JPanel {
    JPanel userListArea;
    private static Logger logger = Logger.getLogger(UserListPanel.class.getName());

    public UserListPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it

        userListArea = content;

        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

        });
    }

    // UCID: jeo29
    // Date: April 27, 2024
    protected void recentUser(long clientId) {
        // Iterate through the components in the userListArea
        Component[] cs = userListArea.getComponents();
        for (Component component : cs) {
            // Check if the component is a JEditorPane and its name matches the clientId
            if (component.getName().equals(clientId + " ")) {
                JEditorPane namePane = (JEditorPane) component;
                // Update the HTML content to change text color to red
                String htmlContent = namePane.getText().replace("color=\"black\"", "color=\"" + "red" + "\"");
                namePane.setText(htmlContent);
                namePane.setName(clientId + " ");
                System.out.println("Text color set to red for client: " + clientId);
            }
            else {
                JEditorPane namePane = (JEditorPane) component;
                // Update the HTML content to change text color to black
                String htmlContent = namePane.getText().replace("color=\"red\"", "color=\"" + "black" + "\"");
                namePane.setText(htmlContent);
            }
        }
        userListArea.revalidate();
        userListArea.repaint();
    }


    // UCID: jeo29
    // April 27, 2024
    protected void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);
        JPanel content = userListArea;
        logger.log(Level.INFO, "Userlist: " + content.getSize());
        JEditorPane textContainer = new JEditorPane("text/html", clientName);
        textContainer.setName(clientId + " ");
        String htmlContent = "<html><font color='black'>" + textContainer.getText() + "</font></html>";
        textContainer.setText(htmlContent);
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
                new Dimension(content.getWidth(), ClientUtils.calcHeightForText(this, clientName, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        ClientUtils.clearBackground(textContainer);
        content.add(textContainer);
    }

    // UCID: jeo29
    // Date: April 27, 2024
    protected void updateUserListItem(long clientId, String clientName) {
        // Iterate through the components in the userListArea
        for (Component component : userListArea.getComponents()) {
            // Check if the component is a JEditorPane and its name matches the clientName
            if (component instanceof JEditorPane && component.getName().equals(clientId + " ")) {
                JEditorPane namePane = (JEditorPane) component;
                namePane.setText(clientName);
                namePane.setName(clientId + " ");
                break;
            }
        }
    }

    protected void removeUserListItem(long clientId) {
        logger.log(Level.INFO, "removing user list item for id " + clientId);
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                userListArea.remove(c);
                break;
            }
        }
    }

    protected void clearUserList() {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            userListArea.remove(c);
        }
    }
    public void resizeUserListItems() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resizeUserListItems'");
    }


    
}