package NDFF.Client.Views;

import java.awt.BorderLayout;
import java.awt.Point;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import NDFF.Client.Client;
import NDFF.Common.Card;
import NDFF.Common.Phase;

public class PlayView extends JPanel {
    private final JPanel buttonPanel = new JPanel();
    private final GridUI GridUI;
    private final HandUI HandUI;
    private Point selectedCell;
    private Card selectedCard;

    public PlayView(String name) {
        this.setName(name);
        this.setLayout(new BorderLayout());
        // example user interaction
        buttonPanel.setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(0.6);
        splitPane.setOneTouchExpandable(false);
        splitPane.setEnabled(false); // Prevent user from moving the divider

        GridUI = new GridUI(this::handleCellClick);
        splitPane.setTopComponent(GridUI);

        HandUI = new HandUI(this::handleCardSelection);
        splitPane.setBottomComponent(HandUI);
        buttonPanel.add(splitPane, BorderLayout.CENTER);

        this.add(buttonPanel, BorderLayout.CENTER);

    }

    private void handleCardSelection(Card card) {
        selectedCard = card;
        processSelections();
    }

    private void handleCellClick(Point point) {
        selectedCell = point;
        processSelections();
    }

    private boolean confirmSelection(String message) {
        boolean confirmed = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Action",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        if (!confirmed) {
            resetSelections();
        }
        return confirmed;
    }

    private void processSelections() {
        if (selectedCard != null) {
            try {
                if (selectedCard.requiresCoordinates() && selectedCell != null) {
                    String message = String.format("Use card '%s' at cell (%d, %d)?",
                            selectedCard.getName(), selectedCell.x, selectedCell.y);
                    if (confirmSelection(message)) {
                        Client.INSTANCE.sendUseCard(selectedCard, selectedCell.x, selectedCell.y);
                        resetSelections();
                    }
                } else if (!selectedCard.requiresCoordinates()) {
                    String message = String.format("Use card '%s'?", selectedCard.getName());
                    if (confirmSelection(message)) {
                        Client.INSTANCE.sendUseCard(selectedCard);
                        resetSelections();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (selectedCell != null) {
            String message = String.format("Cast lure at cell (%d, %d)?",
                    selectedCell.x, selectedCell.y);
            if (confirmSelection(message)) {
                try {
                    Client.INSTANCE.sendCast(selectedCell.x, selectedCell.y);
                    resetSelections();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetSelections() {
        selectedCard = null;
        selectedCell = null;
    }

    public void changePhase(Phase phase) {
        if (phase == Phase.READY) {
            buttonPanel.setVisible(false);
        } else if (phase == Phase.IN_PROGRESS) {
            buttonPanel.setVisible(true);
        }
    }

}
