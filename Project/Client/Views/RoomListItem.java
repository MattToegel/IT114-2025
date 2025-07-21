package Project.Client.Views;

import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * RoomListItem represents an item in the list of rooms with a join button.
 */
public class RoomListItem extends JPanel {
    private final JLabel roomName;
    private final JButton joinButton;

    /**
     * Constructs a RoomListItem with the specified room name and callback.
     *
     * @param room   - Name of room to show on the UI.
     * @param onJoin - Callback to trigger when the button is clicked.
     */
    public RoomListItem(String room, Consumer<String> onJoin) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        roomName = new JLabel(room);
        roomName.setToolTipText("Room name");

        joinButton = new JButton("Join");
        joinButton.setToolTipText("Join this room");
        joinButton.addActionListener(_ -> SwingUtilities.invokeLater(() -> onJoin.accept(roomName.getText())));

        add(roomName);
        add(Box.createHorizontalGlue()); // Fills up horizontal space
        add(joinButton);
    }

    /**
     * Gets the room name displayed in this item.
     *
     * @return the room name.
     */
    public String getRoomName() {
        return roomName.getText();
    }
}