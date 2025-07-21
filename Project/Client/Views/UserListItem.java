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
    private final String displayName; // store original name for future features that require formatting changes

    /**
     * Constructor to create a UserListItem.
     *
     * @param clientId    The ID of the client.
     * @param displayName The name of the client.
     */
    public UserListItem(long clientId, String displayName) {
        this.displayName = displayName;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Name (first line)
        textContainer = new JEditorPane("text/html", this.displayName);
        textContainer.setName(Long.toString(clientId));
        textContainer.setEditable(false);
        textContainer.setBorder(new EmptyBorder(0, 0, 0, 0));
        textContainer.setOpaque(false);
        textContainer.setBackground(new Color(0, 0, 0, 0));
        add(textContainer);

        // Second line: indicator + points
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setOpaque(false);

        turnIndicator = new JPanel();
        turnIndicator.setPreferredSize(new Dimension(10, 10));
        turnIndicator.setMinimumSize(turnIndicator.getPreferredSize());
        turnIndicator.setMaximumSize(turnIndicator.getPreferredSize());
        turnIndicator.setOpaque(true);
        turnIndicator.setVisible(true);
        rowPanel.add(turnIndicator);
        rowPanel.add(Box.createHorizontalStrut(8)); // spacing between indicator and points

        pointsPanel = new JEditorPane("text/html", "");
        pointsPanel.setEditable(false);
        pointsPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pointsPanel.setOpaque(false);
        pointsPanel.setBackground(new Color(0, 0, 0, 0));
        rowPanel.add(pointsPanel);

        add(rowPanel);
        setPoints(-1);
    }

    /**
     * Mostly used to trigger a reset, but if used for a true value, it'll apply
     * Color.GREEN
     * 
     * @param didTakeTurn true if the user took their turn
     */
    public void setTurn(boolean didTakeTurn) {
        setTurn(didTakeTurn, Color.GREEN);
    }

    /**
     * Sets the indicator and color based on turn status
     * 
     * @param didTakeTurn if true, applies trueColor; otherwise applies transparent
     * @param trueColor   Color to apply when true
     */
    public void setTurn(boolean didTakeTurn, Color trueColor) {

        turnIndicator.setBackground(didTakeTurn ? trueColor : new Color(0, 0, 0, 0));
        turnIndicator.revalidate();
        turnIndicator.repaint();
        this.revalidate();
        this.repaint();
    }

    /**
     * Sets the points display for this user.
     * 
     * @param points the number of points, or <0 to hide
     */
    public void setPoints(int points) {
        if (points < 0) {
            pointsPanel.setText("0");
            pointsPanel.setVisible(false);
        } else {
            pointsPanel.setText(Integer.toString(points));
            if (!pointsPanel.isVisible()) {
                pointsPanel.setVisible(true);
            }
        }
        repaint();
    }
}