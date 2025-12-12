package Project.Client.Views;

import Project.Client.CardViewName;
import Project.Client.Client;
import Project.Client.Interfaces.ICardControls;
import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IMessageEvents;
import Project.Client.Interfaces.IRoomEvents;
import Project.Common.Constants;
import Project.Common.LoggerUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * ChatView represents the main chat interface where messages can be sent and
 * received.
 * Uses new view registration and naming conventions.
 */
public class ChatView extends JPanel implements IMessageEvents, IConnectionEvents, IRoomEvents {
    private JPanel chatArea = new JPanel(new GridBagLayout());
    private UserListView userListView;
    private final float CHAT_SPLIT_PERCENT = 0.7f;

    public ChatView(ICardControls controls) {
        super(new BorderLayout(10, 10));

        chatArea.setAlignmentY(Component.TOP_ALIGNMENT);
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        userListView = new UserListView();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, userListView);
        splitPane.setResizeWeight(CHAT_SPLIT_PERCENT);
        splitPane.setDividerLocation(CHAT_SPLIT_PERCENT);
        // Enforce splitPane split
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    splitPane.setDividerLocation(CHAT_SPLIT_PERCENT);
                    resizeEditorPanes();
                });

            }

            @Override
            public void componentMoved(ComponentEvent e) {
                resizeEditorPanes();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    splitPane.setDividerLocation(CHAT_SPLIT_PERCENT);
                    resizeEditorPanes();
                });
            }
        });

        JPanel input = new JPanel();
        input.setLayout(new BoxLayout(input, BoxLayout.X_AXIS));
        input.setBorder(new EmptyBorder(5, 5, 5, 5));
        JTextField textValue = new JTextField();
        input.add(textValue);
        JButton button = new JButton("Send");
        textValue.addActionListener(evt -> button.doClick()); // Enter key submits

        // 🔧 FIXED HERE: changed lambda param from `e` to `evt`
        button.addActionListener(evt -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    String text = textValue.getText().trim();
                    if (!text.isEmpty()) {
                        Client.INSTANCE.sendMessage(text);
                        textValue.setText("");
                    }
                } catch (NullPointerException | IOException e) {
                    LoggerUtil.INSTANCE.severe("Error sending message", e);
                }
            });
        });
        input.add(button);

        this.add(splitPane, BorderLayout.CENTER);
        this.add(input, BorderLayout.SOUTH);

        // Register this view with the new mapping
        setName(CardViewName.CHAT.name());
        controls.registerView(CardViewName.CHAT.name(), this);

        // Add vertical glue to push messages to the top
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        chatArea.add(Box.createVerticalGlue(), gbc);

        Client.INSTANCE.registerCallback(this);

    }

    public void addText(String text) {
        SwingUtilities.invokeLater(() -> {
            JEditorPane textContainer = new JEditorPane("text/html", text);
            textContainer.setEditable(false);
            textContainer.setBorder(BorderFactory.createEmptyBorder());
            JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
            int scrollBarWidth = parentScrollPane.getVerticalScrollBar().getPreferredSize().width;
            int availableWidth = chatArea.getWidth() - scrollBarWidth - 10;
            textContainer.setSize(new Dimension(availableWidth, Integer.MAX_VALUE));
            Dimension d = textContainer.getPreferredSize();
            textContainer.setPreferredSize(new Dimension(availableWidth, d.height));
            textContainer.setOpaque(false);
            textContainer.setBorder(BorderFactory.createEmptyBorder());
            textContainer.setBackground(new Color(0, 0, 0, 0));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(0, 0, 5, 5);
            chatArea.add(textContainer, gbc);
            chatArea.revalidate();
            chatArea.repaint();
            if (parentScrollPane != null) {
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = parentScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            }
        });
    }

    private void resizeEditorPanes() {
        JScrollPane parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatArea);
        int scrollBarWidth = parentScrollPane.getVerticalScrollBar().getPreferredSize().width;
        int width = chatArea.getParent().getWidth() - scrollBarWidth - 10;

        for (Component comp : chatArea.getComponents()) {
            if (comp instanceof JEditorPane) {
                JEditorPane editorPane = (JEditorPane) comp;
                editorPane.setSize(new Dimension(width, Integer.MAX_VALUE));
                Dimension d = editorPane.getPreferredSize();
                editorPane.setPreferredSize(new Dimension(width, d.height));
                editorPane.revalidate();
            }
        }
        chatArea.revalidate();
        chatArea.repaint();

    }

    @Override
    public void onMessageReceive(long clientId, String message) {
        if (clientId < Constants.DEFAULT_CLIENT_ID) {
            return;
        }
        String displayName = Client.INSTANCE.getDisplayNameFromId(clientId);
        // added color to differentiate between room and user messages
        String name = clientId == Constants.DEFAULT_CLIENT_ID ? "<font color=blue>%s</font>"
                : "<font color=purple>%s</font>";
        name = String.format(name, displayName);
        addText(String.format("%s: %s", name, message));
    }

    @Override
    public void onClientDisconnect(long clientId) {

        boolean isMe = Client.INSTANCE.isMyClientId(clientId);
        String message = String.format("*%s disconnected*",
                isMe ? "You" : Client.INSTANCE.getDisplayNameFromId(clientId));
        addText(message);
    }

    @Override
    public void onReceiveClientId(long id) {
        addText("*You connected*");
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        if (clientId == Constants.DEFAULT_CLIENT_ID) {
            return;
        }

        if (!isQuiet) {
            String displayName = Client.INSTANCE.getDisplayNameFromId(clientId);
            boolean isMe = Client.INSTANCE.isMyClientId(clientId);
            // Example 1: Client generated join/leave message (see Room.java for Example 2)
            String message = String.format("<font color=blue>*%s %s the Room %s*</font>",
                    /* 1st %s */ isMe ? "You" : displayName,
                    /* 2nd %s */ isJoin ? "joined" : "left",
                    /* 3rd %s */ roomName == null ? "" : roomName); // added handling of null after the demo video
            addText(message);
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // unused
    }
}
