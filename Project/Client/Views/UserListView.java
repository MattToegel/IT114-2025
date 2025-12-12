package Project.Client.Views;

import Project.Client.Client;
import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IPhaseEvent;
import Project.Client.Interfaces.IPointsEvent;
import Project.Client.Interfaces.IReadyEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Interfaces.ITurnEvent;
import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * AJM292 - 12/10/2025
 * UserListView represents a UI component that displays a list of users.
 * This version keeps a map of user rows and always renders them
 * sorted by points (desc), then name (asc).
 */
public class UserListView extends JPanel
        implements IConnectionEvents, IRoomEvents, IReadyEvent, IPointsEvent, ITurnEvent, IPhaseEvent {

    private final JPanel userListArea;
    private final GridBagConstraints lastConstraints; // constraints for the glue at the bottom
    private final HashMap<Long, UserListItem> userItemsMap; // Maintain a map of client IDs to UserListItems

    // Track last phase so we can detect end-of-game transition
    private Phase lastPhase = Phase.READY;

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

        // Vertical glue to push items to the top
        lastConstraints = new GridBagConstraints();
        lastConstraints.gridx = 0;
        lastConstraints.gridy = GridBagConstraints.RELATIVE;
        lastConstraints.weighty = 1.0;
        lastConstraints.fill = GridBagConstraints.VERTICAL;
        userListArea.add(Box.createVerticalGlue(), lastConstraints);

        Client.INSTANCE.registerCallback(this);
    }

    /**
     * AJM292 - 12/10/2025
     * Adds a user row to the internal map and refreshes the sorted layout.
     */
    private void addUserListItem(long clientId, String clientName) {
        SwingUtilities.invokeLater(() -> {
            if (userItemsMap.containsKey(clientId)) {
                LoggerUtil.INSTANCE.warning("User already in the list: " + clientName);
                return;
            }
            LoggerUtil.INSTANCE.info("Adding user to list: " + clientName);
            UserListItem userItem = new UserListItem(clientId, clientName);
            userItemsMap.put(clientId, userItem);
            refreshUserListLayoutSorted();
        });
    }

    /**
     * AJM292 - 12/10/2025
     * Removes a user row by id and refreshes layout.
     */
    private void removeUserListItem(long clientId) {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Removing user list item for id " + clientId);
            try {
                userItemsMap.remove(clientId);
                refreshUserListLayoutSorted();
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error removing user list item", e);
            }
        });
    }

    /**
     * AJM292 - 12/10/2025
     * Clears all user rows and refreshes layout.
     */
    private void clearUserList() {
        SwingUtilities.invokeLater(() -> {
            LoggerUtil.INSTANCE.info("Clearing user list");
            try {
                userItemsMap.clear();
                refreshUserListLayoutSorted();
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error clearing user list", e);
            }
        });
    }

    /**
     * AJM292 - 12/10/2025
     * Rebuilds the panel contents sorted by:
     *  - points descending
     *  - then display name ascending
     */
    private void refreshUserListLayoutSorted() {
        SwingUtilities.invokeLater(() -> {
            try {
                userListArea.removeAll();

                // Build list from map
                List<UserListItem> items = new ArrayList<>(userItemsMap.values());

                // Sort by points (desc), then name (asc, case-insensitive)
                items.sort(Comparator
                        .comparingInt((UserListItem u) -> {
                            int pts = u.getPoints();
                            // treat negative as 0 so "unset" scores go to the bottom nicely
                            return pts < 0 ? 0 : pts;
                        })
                        .reversed()
                        .thenComparing(u -> u.getDisplayName().toLowerCase()));

                // Re-add all items with proper constraints
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.weightx = 1;
                gbc.anchor = GridBagConstraints.NORTH;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.insets = new Insets(0, 0, 5, 5);

                for (UserListItem item : items) {
                    gbc.gridy = userListArea.getComponentCount();
                    userListArea.add(item, gbc);
                }

                // Add bottom glue again
                userListArea.add(Box.createVerticalGlue(), lastConstraints);

                userListArea.revalidate();
                userListArea.repaint();
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error refreshing sorted user list layout", e);
            }
        });
    }

    /**
     * Shows a dialog with the final scoreboard (sorted by points then name).
     */
    public void showScoreboard() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Build sorted list
                List<UserListItem> items = new ArrayList<>(userItemsMap.values());

                items.sort(Comparator
                        .comparingInt((UserListItem u) -> {
                            int pts = u.getPoints();
                            return pts < 0 ? 0 : pts;
                        })
                        .reversed()
                        .thenComparing(u -> u.getDisplayName().toLowerCase()));

                if (items.isEmpty()) {
                    LoggerUtil.INSTANCE.info("Scoreboard requested but user list is empty");
                    return;
                }

                Window parentWindow = SwingUtilities.getWindowAncestor(this);
                JDialog dialog = new JDialog(parentWindow, "Final Scoreboard", Dialog.ModalityType.APPLICATION_MODAL);

                JPanel content = new JPanel();
                content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                content.setBorder(new EmptyBorder(10, 10, 10, 10));

                int rank = 1;
                for (UserListItem item : items) {
                    int pts = item.getPoints();

                    JLabel rowLabel = new JLabel(
                            "#" + rank + " - " + item.getDisplayName() + " (" + pts + " pts)");

                    // highlight the winner
                    if (rank == 1) {
                        rowLabel.setFont(rowLabel.getFont().deriveFont(java.awt.Font.BOLD));
                        rowLabel.setForeground(new Color(0, 200, 0));
                    }

                    content.add(rowLabel);
                    content.add(Box.createVerticalStrut(4));
                    rank++;
                }

                dialog.getContentPane().add(new JScrollPane(content));
                dialog.pack();
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);

            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error showing scoreboard", e);
            }
        });
    }

    // ====================
    // IPhaseEvent handling
    // ====================

    @Override
    public void onReceivePhase(Phase phase) {
        SwingUtilities.invokeLater(() -> {
            try {
                LoggerUtil.INSTANCE.info("UserListView received phase: " + phase);

                // Detect transition from IN_PROGRESS -> READY (end of game)
                if (phase == Phase.READY && lastPhase == Phase.IN_PROGRESS) {
                    // Update our tracker first so we don't double-trigger
                    lastPhase = phase;
                    showScoreboard();
                } else {
                    lastPhase = phase;
                }
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error handling phase in UserListView", e);
            }
        });
    }

    // ========================
    // Interface implementations
    // ========================

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
                userItemsMap.values().forEach(u -> u.setTurn(false)); // reset all
                // turn state can affect visuals; keep layout consistent
                refreshUserListLayoutSorted();
            });
        } else if (userItemsMap.containsKey(clientId)) {
            SwingUtilities.invokeLater(() -> {
                userItemsMap.get(clientId).setTurn(didtakeCurn);
                // if you ever want to sort by "pending" status too, this is the hook
                refreshUserListLayoutSorted();
            });
        }
    }

    @Override
    public void onPointsUpdate(long clientId, int points) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            SwingUtilities.invokeLater(() -> {
                try {
                    userItemsMap.values().forEach(u -> u.setPoints(-1)); // reset all
                    refreshUserListLayoutSorted();
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error resetting user items", e);
                }
            });
        } else if (userItemsMap.containsKey(clientId)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    userItemsMap.get(clientId).setPoints(points);
                    refreshUserListLayoutSorted();
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
                    userItemsMap.values().forEach(u -> u.setTurn(false)); // reset all
                    refreshUserListLayoutSorted();
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error resetting user items", e);
                }
            });
        } else if (userItemsMap.containsKey(clientId)) {

            SwingUtilities.invokeLater(() -> {
                try {
                    LoggerUtil.INSTANCE.info("Setting user item ready for id " + clientId + " to " + isReady);
                    userItemsMap.get(clientId).setTurn(isReady, Color.GRAY);
                    refreshUserListLayoutSorted();
                } catch (Exception e) {
                    LoggerUtil.INSTANCE.severe("Error setting user item", e);
                }
            });
        }
    }
}
