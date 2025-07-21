package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import Project.Common.RoomAction;
import Project.Client.CardViewName;
import Project.Client.Client;
import Project.Client.Interfaces.ICardControls;
import Project.Client.Interfaces.IRoomEvents;
import Project.Common.LoggerUtil;

/**
 * RoomsView class represents the UI for managing chat rooms.
 */
public class RoomsView extends JPanel implements IRoomEvents {
    private final JPanel container;
    private final List<RoomListItem> rooms = new ArrayList<>();
    private final JLabel message;

    /**
     * Constructor to create the RoomsView UI.
     *
     * @param controls The card controls interface to handle navigation.
     */
    public RoomsView(ICardControls controls) {
        super(new BorderLayout(10, 10));
        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 0),
                new EmptyBorder(10, 10, 0, 10)));

        JScrollPane scroll = new JScrollPane(container, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JButton back = new JButton("Close");
        back.addActionListener(_ -> controls.previousView());

        JPanel search = new JPanel();
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));
        search.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel searchContent = new JPanel();
        searchContent.setLayout(new BoxLayout(searchContent, BoxLayout.X_AXIS));
        JLabel searchLabel = new JLabel("Room Name");
        JTextField searchValue = new JTextField();
        JButton searchButton = new JButton("Search");
        message = new JLabel("", 0);
        JPanel messageContainer = new JPanel();
        messageContainer.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Search button action
        searchButton.addActionListener(_ -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        removeAllRooms();
                        Client.INSTANCE.sendRoomAction(query, RoomAction.LIST);
                        message.setText("Sent query");
                    } else {
                        message.setText("Can't search with an empty query");
                    }
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Error sending request: " + e.getMessage(), e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        JButton createButton = new JButton("Create");
        createButton.addActionListener(_ -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        Client.INSTANCE.sendRoomAction(query, RoomAction.CREATE);
                        message.setText("Created room");
                    } else {
                        message.setText("Can't create a room without a name");
                    }
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Error sending request: " + e.getMessage(), e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        JButton joinButton = new JButton("Join");
        joinButton.addActionListener(_ -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String query = searchValue.getText().trim();
                    if (!query.isEmpty()) {
                        Client.INSTANCE.sendRoomAction(query, RoomAction.JOIN);
                        message.setText("Joined room");
                    } else {
                        message.setText("Can't join a room without a name");
                    }
                } catch (NullPointerException ne) {
                    message.setText("Not connected");
                } catch (IOException e) {
                    LoggerUtil.INSTANCE.warning("Not connected", e);
                    message.setText("Error sending request: " + e.getMessage());
                }
            });
        });

        searchContent.add(searchLabel);
        searchContent.add(Box.createHorizontalStrut(5));
        searchContent.add(searchValue);
        searchContent.add(Box.createHorizontalStrut(5));
        searchContent.add(searchButton);
        searchContent.add(Box.createHorizontalStrut(5));
        searchContent.add(createButton);
        searchContent.add(Box.createHorizontalStrut(5));
        searchContent.add(joinButton);
        search.add(searchContent);
        messageContainer.add(message);
        search.add(messageContainer);

        this.add(search, BorderLayout.NORTH);
        this.add(back, BorderLayout.SOUTH);
        this.add(scroll, BorderLayout.CENTER);

        this.setName(CardViewName.ROOMS.name());
        controls.registerView(CardViewName.ROOMS.name(), this);

        Client.INSTANCE.registerCallback(this);
    }

    /**
     * Sets the message text displayed in the panel.
     *
     * @param message The message text to set.
     */
    private void setMessage(String message) {
        this.message.setText(message);
    }

    /**
     * Adds a room to the rooms list.
     *
     * @param room The name of the room to add.
     */
    private void addRoom(String room) {
        if (room != null) {
            LoggerUtil.INSTANCE.info("Adding: " + room);
            RoomListItem roomListItem = new RoomListItem(room, this::handleSelection);
            roomListItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            container.add(roomListItem);
            rooms.add(roomListItem);
            revalidate();
            repaint();
        }
    }

    /**
     * Removes a room from the rooms list.
     *
     * @param room The name of the room to remove.
     */
    public void removeRoom(String room) {
        rooms.removeIf(r -> {
            if (r.getRoomName().equalsIgnoreCase(room)) {
                r.removeAll();
                container.remove(r);
                revalidate();
                repaint();
                return true;
            }
            return false;
        });
    }

    /**
     * Removes all rooms from the rooms list.
     */
    public void removeAllRooms() {
        LoggerUtil.INSTANCE.info("Clearing rooms");
        for (RoomListItem roomListItem : rooms) {
            LoggerUtil.INSTANCE.info("Removing " + roomListItem.getRoomName());
            container.remove(roomListItem);
        }
        rooms.clear();
        revalidate();
        repaint();
    }

    /**
     * Handles the selection of a room by sending a join request.
     *
     * @param room The name of the room to join.
     */
    public void handleSelection(String room) {
        SwingUtilities.invokeLater(() -> {
            try {
                Client.INSTANCE.sendRoomAction(room, RoomAction.JOIN);
            } catch (IOException e) {
                LoggerUtil.INSTANCE.severe("Error joining room: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        removeAllRooms();
        if (message != null && !message.isEmpty()) {
            setMessage(message);
        } else {
            setMessage("Found Rooms: " + (rooms != null ? rooms.size() : 0));
        }
        if (rooms != null) {
            for (String room : rooms) {
                addRoom(room);
            }
        }
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        // unused
    }
}
