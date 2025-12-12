package Project.Client.Views;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * UserListItem represents a user entry in the user list.
 */
public class UserListItem extends JPanel {

    private final JEditorPane textContainer;
    private final JPanel turnIndicator;
    private final JEditorPane pointsPanel;

    private final String displayName; // original name for sorting & formatting

    // NEW FIELD → required for sorting, scoreboard, and refresh logic
    private int points = -1;

    /**
     * Constructor to create a UserListItem.
     *
     * @param clientId    The ID of the client.
     * @param displayName The name of the client.
     */
    public UserListItem(long clientId, String displayName) {
        this.displayName = displayName;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Top line: User name
        textContainer = new JEditorPane("text/html", this.displayName);
        textContainer.setName(Long.toString(clientId));
        textContainer.setEditable(false);
        textContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
        textContainer.setOpaque(false);
        add(textContainer);

        // Row for turn indicator + points
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);

        // Small square indicator for turn / ready state
        turnIndicator = new JPanel();
        turnIndicator.setPreferredSize(new Dimension(10, 10));
        turnIndicator.setMinimumSize(turnIndicator.getPreferredSize());
        turnIndicator.setMaximumSize(turnIndicator.getPreferredSize());
        turnIndicator.setOpaque(true);
        turnIndicator.setVisible(true);
        rowPanel.add(turnIndicator);

        rowPanel.add(Box.createHorizontalStrut(8)); // spacing

        // Points display
        pointsPanel = new JEditorPane("text/html", "");
        pointsPanel.setEditable(false);
        pointsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pointsPanel.setOpaque(false);
        rowPanel.add(pointsPanel);

        add(rowPanel);

        // By default, hide points until set
        setPoints(-1);
    }

    /**
     * Sets the indicator and color based on turn status.
     */
    public void setTurn(boolean didTakeTurn) {
        setTurn(didTakeTurn, Color.GREEN);
    }

    public void setTurn(boolean didTakeTurn, Color trueColor) {
        turnIndicator.setBackground(didTakeTurn ? trueColor : new Color(0, 0, 0, 0));
        revalidate();
        repaint();
    }

    /**
     * Sets the points display for this user.
     * @param points The number of points, or <0 to hide.
     */
    public void setPoints(int points) {
        this.points = points; // store for sorting + scoreboard

        if (points < 0) {
            pointsPanel.setText("0");
            pointsPanel.setVisible(false);
        } else {
            pointsPanel.setText(Integer.toString(points));
            pointsPanel.setVisible(true);
        }
        repaint();
    }

    // === NEW METHODS FOR UserListView & Scoreboard ===

    /** Used for sorting by score */
    public int getPoints() {
        return points;
    }

    /** Used for sorting by name */
    public String getDisplayName() {
        return displayName;
    }
}
