package Project.Client.Views;

import Project.Client.CardViewName;
import Project.Client.Interfaces.ICardControls;
import Project.Common.LoggerUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

// UserDetailsView lets the user enter their username before connecting.
public class UserDetailsView extends JPanel {
    // Stores the username entered by the user
    private String username;
    // UI components for user input and error display
    private final JTextField userField = new JTextField();
    private final JLabel userError = new JLabel();

    /**
     * Sets up the user details view UI for entering a username.
     * Registers this view with the card controls for navigation.
     */
    public UserDetailsView(ICardControls controls) {
        super(new BorderLayout(10, 10));

        // Set panel name and register
        setName(CardViewName.USER_INFO.name());
        controls.registerView(CardViewName.USER_INFO.name(), this);

        // Main content panel with vertical layout and padding
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Username input section
        content.add(new JLabel("Username: "));
        content.add(userField);
        userError.setVisible(false);
        content.add(userError);
        content.add(Box.createRigidArea(new Dimension(0, 200)));

        // Previous and Connect buttons for navigation
        JButton previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> controls.previousView());
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> onConnect(controls));

        JPanel buttons = new JPanel();
        buttons.add(previousButton);
        buttons.add(connectButton);

        // Push the buttons to the bottom of the content panel
        content.add(Box.createVerticalGlue());
        content.add(buttons);

        add(content, BorderLayout.CENTER);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /**
     * Handles the Connect button click: validates username input and, if valid,
     * saves the username,
     * logs it, and triggers the connect action.
     */
    private void onConnect(ICardControls controls) {
        String incomingUsername = userField.getText().trim();
        if (incomingUsername.isEmpty()) {
            userError.setText("Username must be provided");
            userError.setVisible(true);
        } else {
            username = incomingUsername;
            LoggerUtil.INSTANCE.info("Chosen username: " + username);
            userError.setVisible(false);
            controls.connect();
        }
    }

    // Getter for the username entered by the user
    public String getUsername() {
        return username;
    }
}
