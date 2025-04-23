package Equals6.Client.Views;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import Equals6.Client.CardView;
import Equals6.Client.Client;
import Equals6.Client.Interfaces.ICardControls;
import Equals6.Client.Interfaces.IPhaseEvent;
import Equals6.Client.Interfaces.IRoomEvents;
import Equals6.Common.Card;
import Equals6.Common.Constants;
import Equals6.Common.LoggerUtil;
import Equals6.Common.Phase;

public class GamePanel extends JPanel implements IRoomEvents, IPhaseEvent {

    private JPanel playPanel;
    private CardLayout cardLayout;
    private static final String READY_PANEL = "READY";
    private static final String PLAY_PANEL = "PLAY";// example panel for this lesson
    private Point selectedCell = new Point(-1, -1);
    private Card selectedCard = null;

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
        HandPanel handPanel = new HandPanel(this::handleCardSelection);
        handPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        // handPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));
        playPanel.setLayout(new BorderLayout());
        playPanel.add(gridPanel, BorderLayout.CENTER);
        playPanel.add(handPanel, BorderLayout.SOUTH);
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
        controls.addPanel(CardView.CHAT_GAME_SCREEN.name(), this);
        setVisible(false);
    }

    private void handleCellSelection(Point p) {
        selectedCell.setLocation(p.getX(), p.getY());
        processChoices();
    }

    private void handleCardSelection(Card card) {
        selectedCard = card;
        processChoices();
    }

    private void processChoices() {
        if (selectedCell.getX() < 0 || selectedCell.getY() < 0 || selectedCard == null) {
            LoggerUtil.INSTANCE.warning("[Debugging] Incomplete cell or card selection");
            return;
        }
        // log the coordinates and card
        LoggerUtil.INSTANCE.info("Selected cell: (" + selectedCell.getX() + ", " + selectedCell.getY() + ")");
        LoggerUtil.INSTANCE.info("Selected card: " + selectedCard.getName());
        try {
            Client.INSTANCE.sendCardChoice((int) selectedCell.getX(), (int) selectedCell.getY(), selectedCard);
            selectedCell.setLocation(-1, -1);
            selectedCard = null;
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