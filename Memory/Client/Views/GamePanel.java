package Memory.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Memory.Client.CardView;
import Memory.Client.Client;
import Memory.Client.Interfaces.ICardControls;
import Memory.Client.Interfaces.IPhaseEvent;
import Memory.Client.Interfaces.IRoomEvents;
import Memory.Common.Constants;
import Memory.Common.Phase;

public class GamePanel extends JPanel implements IRoomEvents, IPhaseEvent {

    private JPanel playPanel;
    private CardLayout cardLayout;
    private static final String READY_PANEL = "READY";
    private static final String PLAY_PANEL = "PLAY";// example panel for this lesson

    @SuppressWarnings("unused")
    public GamePanel(ICardControls controls) {
        super(new BorderLayout());

        JPanel gameContainer = new JPanel(new CardLayout());
        cardLayout = (CardLayout) gameContainer.getLayout();
        this.setName(CardView.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);

        ReadyPanel readyPanel = new ReadyPanel();
        readyPanel.setName(READY_PANEL);
        gameContainer.add(READY_PANEL, readyPanel);

        playPanel = new JPanel();
        playPanel.setName(PLAY_PANEL);
        GridPanel gridPanel = new GridPanel(this::handleCellSelection);
        // handPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));
        playPanel.setLayout(new BorderLayout());
        playPanel.add(gridPanel, BorderLayout.CENTER);
        gameContainer.add(PLAY_PANEL, playPanel);

        GameEventsPanel gameEventsPanel = new GameEventsPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, gameContainer, gameEventsPanel);
        splitPane.setResizeWeight(0.7);

        playPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                splitPane.setDividerLocation(0.7);
            }
        });

        playPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                playPanel.revalidate();
                playPanel.repaint();
            }
        });

        this.add(splitPane, BorderLayout.CENTER);

        JPanel interactions = new JPanel();
        JButton awayButton = new JButton("Toggle Away");
        awayButton.addActionListener(event -> {
            try {
                Client.INSTANCE.sendAway();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        interactions.add(awayButton);
        this.add(interactions, BorderLayout.SOUTH);

        controls.addPanel(CardView.CHAT_GAME_SCREEN.name(), this);
        setVisible(false);
    }

    private void handleCellSelection(Point p) {
        try {
            Client.INSTANCE.sendPickLocation((int) p.getX(), (int) p.getY());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        if (Constants.LOBBY.equals(roomName) && isJoin) {
            setVisible(false);
            revalidate();
            repaint();
        }
    }

    @Override
    public void onReceivePhase(Phase phase) {
        System.out.println("Received phase: " + phase.name());
        if (!isVisible()) {
            setVisible(true);
            getParent().revalidate();
            getParent().repaint();
            System.out.println("GamePanel visible");
        }
        if (phase == Phase.READY) {
            cardLayout.show(playPanel.getParent(), READY_PANEL);
        } else if (phase == Phase.IN_PROGRESS) {
            cardLayout.show(playPanel.getParent(), PLAY_PANEL);
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // Not used here, but needs to be defined due to interface
    }
}