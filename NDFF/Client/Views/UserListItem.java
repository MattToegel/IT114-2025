package NDFF.Client.Views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import NDFF.Client.Client;
import NDFF.Common.LoggerUtil;
import NDFF.Common.Phase;

/**
 * UserListItem represents a user entry in the user list.
 */
public class UserListItem extends JPanel {
    private final JEditorPane textContainer;
    private final JPanel turnIndicator;
    private final JEditorPane pointsPanel;
    private final String displayName; // store original name for future features that require formatting changes
    private boolean isReady = false; // Track if the user is ready
    private boolean isAway = false; // Track if the user is away
    private static boolean inProgress = false; // Static flag to indicate if the game is in progress
    private final JButton awayButton;
    private long clientId;

    /**
     * Constructor to create a UserListItem.
     *
     * @param clientId    The ID of the client.
     * @param displayName The name of the client.
     */
    public UserListItem(long clientId, String displayName) {
        this.displayName = displayName;
        this.clientId = clientId;
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
        // small JButton used as away toggle
        awayButton = new JButton("Away");
        awayButton.setPreferredSize(new Dimension(30, 10));
        awayButton.setFont(awayButton.getFont().deriveFont(8f)); // 8pt font size
        awayButton.setMargin(new Insets(1, 2, 1, 2));
        awayButton.addActionListener(_ -> {
            // don't set away directly, use the server reply
            try {
                Client.INSTANCE.sendAwayAction();// using it as a toggle
            } catch (IOException e) {
                // example of user feedback via a UI component
                JOptionPane.showMessageDialog(this, "Failed to send away action.", "Error", JOptionPane.ERROR_MESSAGE);
                LoggerUtil.INSTANCE.severe("Error sending away action", e);
            }

        });
        awayButton.setVisible(false); // initially hidden
        rowPanel.add(awayButton);

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

    public void setReady(boolean isReady) {
        this.isReady = isReady;
        // Optionally, you can change the text color or style to indicate readiness
        setTurn(isReady, Color.GRAY);// use existing approach
        applyStatusStyles(); // Apply styles based on readiness
    }

    /**
     * Uses recorded ready status and static phase reference to apply styles to list
     * item
     */
    private void applyStatusStyles() {
        // remove formatting
        textContainer.setText(displayName);
        // !isReady applies yellow text, isAway applies gray background
        StringBuilder html = new StringBuilder("<html>");
        if (!isReady && UserListItem.inProgress) {
            html.append("<font color='yellow'>");
        }
        if (isAway) {
            html.append("<span style='background-color: gray;'>");
        }
        html.append(displayName);
        if (isAway) {
            html.append("</span>");
        }
        if (!isReady && UserListItem.inProgress) {
            html.append("</font>");
        }
        html.append("</html>");
        textContainer.setText(html.toString());
    }

    /**
     * Called from UserListView on phase change
     * 
     * @param phase
     */
    public void setPhase(Phase phase) {
        // Apply it statically since phase is the same across all list items
        UserListItem.inProgress = phase.ordinal() > Phase.READY.ordinal();
        applyStatusStyles();
        repaint();
    }

    public void setAway(boolean isAway) {
        this.isAway = isAway;
        applyStatusStyles();
        // use the server reply as confirmation
        awayButton.setText(isAway ? "Back" : "Away");
        repaint();
    }

    public void toggleGameUI(boolean show) {
        // points and indicators should be handled already
        // this is mostly for the away button and any other UI elements you might add
        if (Client.INSTANCE.isMyClientId(clientId)) {
            // UI items only visible to this client
            awayButton.setVisible(show);
        }
    }
}