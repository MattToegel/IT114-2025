package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Project.Client.CardViewName;
import Project.Client.Client;
import Project.Client.Interfaces.ICardControls;
import Project.Client.Interfaces.IPhaseEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Common.Constants;
import Project.Common.Phase;

public class ChatGameView extends JPanel implements IRoomEvents, IPhaseEvent {
    private final ChatView chatView;
    private final GameView gameView;
    private final JSplitPane splitPane;

    public ChatGameView(ICardControls controls) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // Set panel name and register for card navigation
        setName(CardViewName.CHAT_GAME_SCREEN.name());
        controls.registerView(CardViewName.CHAT_GAME_SCREEN.name(), this);

        chatView = new ChatView(controls);
        gameView = new GameView(controls);
        gameView.setVisible(false);
        gameView.setBackground(Color.BLUE);
        chatView.setBackground(Color.GRAY);

        // JSplitPane is always present; we toggle gameView visibility for mode
        // switching
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gameView, chatView);
        splitPane.setResizeWeight(0.6); // 60% for game, 40% for chat when both are visible
        splitPane.setOneTouchExpandable(false);
        splitPane.setEnabled(false); // Prevent user from moving the divider

        add(splitPane, BorderLayout.CENTER);
        gameView.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(0.6);
            }
        });
        // Start with chat only (gameView hidden, all space to chat)
        showChatOnlyView();
        Client.INSTANCE.registerCallback(this);
    }

    /**
     * Shows both game and chat views in a split pane.
     * Sets divider to 60% for gameView, 40% for chatView.
     */
    public void showGameView() {
        gameView.setVisible(true); // Make gameView visible
        splitPane.setDividerLocation(0.6); // Restore split
    }

    /**
     * Hides the game view and gives all space to chatView.
     * The split pane remains, but gameView is invisible and divider is at far
     * right.
     */
    public void showChatOnlyView() {
        gameView.setVisible(false); // Hide gameView
        chatView.setVisible(true); // Ensure chatView is visible
        splitPane.setDividerLocation(1.0); // All space to chatView
        revalidate();
        repaint(); // Refresh the view

    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // unused
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        if (isJoin && Constants.LOBBY.equals(roomName)) {
            showChatOnlyView();
        }

    }

    @Override
    public void onReceivePhase(Phase phase) {
        showGameView();

    }
}
