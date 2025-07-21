package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Project.Client.Client;
import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IPointsEvent;
import Project.Client.Interfaces.IReadyEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Interfaces.ITurnEvent;
import Project.Common.Constants;
import Project.Common.LoggerUtil;

/**
 * UserListView represents a UI component that displays a list of users.
 */
public class UserListView extends JPanel
        implements IConnectionEvents, IRoomEvents, IReadyEvent, IPointsEvent, ITurnEvent {
    private final JPanel userListArea;
    private final GridBagConstraints lastConstraints; // Keep track of the last constraints for the glue
    private final HashMap<Long, UserListItem> userItemsMap; // Maintain a map of client IDs to UserListItems

    public UserListView() {
        super(new BorderLayout(10, 10));
        userItemsMap = new HashMap<>();

        JPanel content = new JPanel(new GridBagLayout());
        userListArea = content;

        JScrollPane scroll = new JScrollPane(userListArea);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.add(scroll, BorderLayout.CENTER);

        // Add vertical glue to push items to the top
        lastConstraints = new GridBagConstraints();
        lastConstraints.gridx = 0;
        lastConstraints.gridy = GridBagConstraints.RELATIVE;
        lastConstraints.weighty = 1.0;
        lastConstraints.fill = GridBagConstraints.VERTICAL;
        userListArea.add(Box.createVerticalGlue(), lastConstraints);
        Client.INSTANCE.registerCallback(this);
    }

    /**
     * Adds a user to the list.
     */
    private void addUserListItem(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            if (userItemsMap.containsKey(clientId)) {
                LoggerUtil.INSTANCE.warning("User already in the list: " + clientName);
                return;
            }
            LoggerUtil.INSTANCE.info("Adding user to list: " + clientName);
            UserListItem userItem = new UserListItem(clientId, clientName);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = userListArea.getComponentCount() - 1;
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 5, 5);
            // Remove the last glue component if it exists
            if (lastConstraints != null) {
                int index = userListArea.getComponentCount() - 1;
                if (index > -1) {
                    userListArea.remove(index);
                }
            }
            userListArea.add(userItem, gbc);
            userListArea.add(Box.createVerticalGlue(), lastConstraints);
            userItemsMap.put(clientId, userItem);
            userListArea.revalidate();
            userListArea.repaint();
        });
    }

    /**
     * Removes a user from the list.
     */
    private void removeUserListItem(long clientId) {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Removing user list item for id " + clientId);
            try {
                UserListItem item = userItemsMap.remove(clientId);
                if (item != null) {
                    userListArea.remove(item);
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error removing user list item", e);
            }
        });
    }

    /**
     * Clears the user list.
     */
    private void clearUserList() {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Clearing user list");
            try {
                userItemsMap.clear();
                userListArea.removeAll();
                userListArea.revalidate();
                userListArea.repaint();
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error clearing user list", e);
            }
        });
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // unused
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            clearUserList();
            return;
        }
        String displayName = Client.INSTANCE.getDisplayNameFromId(clientId);
        if (isJoin) {
            addUserListItem(clientId, displayName);
        } else {
            removeUserListItem(clientId);
        }
    }

    @Override
    public void onClientDisconnect(long clientId) {
        removeUserListItem(clientId);
    }

    @Override
    public void onReceiveClientId(long id) {
        // unused
    }

    @Override
    public void onTookTurn(long clientId, boolean didtakeCurn) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            SwingUtilities.invokeLater(() -> {
                userItemsMap.values().forEach(u -> u.setTurn(false));// reset all
            });
        } else if (userItemsMap.containsKey(clientId)) {
            SwingUtilities.invokeLater(() -> {
                userItemsMap.get(clientId).setTurn(didtakeCurn);
            });
        }
    }

    @Override
    public void onPointsUpdate(long clientId, int points) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            SwingUtilities.invokeLater(() -> {
                try {
                    userItemsMap.values().forEach(u -> u.setPoints(-1));// reset all
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error resetting user items", e);
                }
            });
        } else if (userItemsMap.containsKey(clientId)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    userItemsMap.get(clientId).setPoints(points);
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error setting user item", e);
                }

            });
        }
    }

    @Override
    public void onReceiveReady(long clientId, boolean isReady, boolean isQuiet) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            SwingUtilities.invokeLater(() -> {
                try {
                    userItemsMap.values().forEach(u -> u.setTurn(false));// reset all
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error resetting user items", e);
                }
            });
        } else if (userItemsMap.containsKey(clientId)) {

            SwingUtilities.invokeLater(() -> {
                try {
                    LoggerUtil.INSTANCE.info("Setting user item ready for id " + clientId + " to " + isReady);
                    userItemsMap.get(clientId).setTurn(isReady, Color.GRAY);
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error setting user item", e);
                }
            });
        }
    }
}
