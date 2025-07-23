package NDFF.Sandbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import NDFF.Common.Card;
import NDFF.Common.CardType;

public class PlayViewSandbox {
    // CardUI
    public static class CardUI extends JButton {
        public CardUI(Card card, Consumer<Card> onSelect) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(8, 8, 8, 8));
            setToolTipText(card.getDescription());

            setText(String.format(
                    "<html><div style='width:100%%; white-space:normal;'><ul style='margin:0;padding:0;list-style:none;'>"
                            + "<li><b>Type:</b> %s</li>"
                            + "<li><b>Value:</b> %.2f</li>"
                            + "<li><b>Description:</b> %s</li>"
                            + "<li><b>Requires Coordinate:</b> %s</li>"
                            + "</ul></div></html>",
                    card.getName(),
                    card.getValue(),
                    card.getDescription(),
                    card.requiresCoordinates() ? "Yes" : "No"));
            addActionListener(_ -> {
                if (onSelect != null) {
                    onSelect.accept(card);
                }
            });
            int cardWidth = 120;
            int cardHeight = 120; // for example
            setPreferredSize(new Dimension(cardWidth, cardHeight));
            setMaximumSize(new Dimension(cardWidth, cardHeight));
            setMinimumSize(new Dimension(cardWidth, cardHeight));
            setAlignmentY(TOP_ALIGNMENT);

        }

        public void removeListeners() {
            for (ActionListener al : getActionListeners()) {
                removeActionListener(al);
            }
        }
    }

    // CellUI
    public static class CellUI extends JPanel {
        private int fishCount = 0;
        private Color highlightColor = null;
        private JLabel infoLabel;

        public CellUI(int x, int y, Consumer<Point> onClick) {
            setBackground(Color.CYAN);
            setBorder(new LineBorder(Color.DARK_GRAY, 1));
            infoLabel = new JLabel();
            add(infoLabel);
            refresh();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) {
                        onClick.accept(new Point(x, y));
                    }
                }
            });
        }

        public void setFishCount(int count) {
            this.fishCount = count;
            refresh();
        }

        public void setHighlight(Color color) {
            this.highlightColor = color;
            refresh();
        }

        private void refresh() {
            String info = String.format("<html><center>Fish: %d</center></html>", fishCount);
            infoLabel.setText(info);
            if (highlightColor != null) {
                setBackground(highlightColor);
            } else {
                setBackground(Color.CYAN);
            }
            repaint();
        }
    }

    // GridUI
    public static class GridUI extends JPanel {
        private int rows;
        private int cols;
        private CellUI[][] cellViews;
        private final Consumer<Point> onCellClick;
        private JPanel container = new JPanel();

        public GridUI(int rows, int cols, Consumer<Point> onCellClick) {
            this.rows = rows;
            this.cols = cols;
            this.onCellClick = onCellClick;
            this.setLayout(new BorderLayout());
            this.add(container, BorderLayout.CENTER);
            generateGrid(rows, cols);
        }

        private void generateGrid(int rows, int cols) {
            container.removeAll();
            cellViews = new CellUI[rows][cols];
            container.setLayout(new GridLayout(rows, cols, 2, 2));
            Dimension preferredSize = new Dimension(16, 16);
            Random rng = new Random();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    CellUI CellUI = new CellUI(r, c, this::handleCellClick);
                    CellUI.setPreferredSize(preferredSize);
                    // Set a random fish count for mock
                    CellUI.setFishCount(rng.nextInt(4));
                    cellViews[r][c] = CellUI;
                    container.add(CellUI);
                }
            }
            container.revalidate();
            container.repaint();
            this.revalidate();
            this.repaint();
        }

        private void handleCellClick(Point point) {
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    cellViews[r][c].setHighlight(null);
            cellViews[point.x][point.y].setHighlight(Color.CYAN);
            if (onCellClick != null)
                onCellClick.accept(point);
        }
    }

    // HandUI
    public static class HandUI extends JPanel {
        private final HashMap<String, CardUI> cards;
        private final Consumer<Card> onCardSelect;
        private final JPanel cardPanel;

        public HandUI(List<Card> cardList, Consumer<Card> onCardSelect) {
            super(new BorderLayout());
            this.onCardSelect = onCardSelect;
            cards = new HashMap<>();

            cardPanel = new JPanel(new GridBagLayout());
            cardPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            JScrollPane scrollPane = new JScrollPane(cardPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            scrollPane.setBorder(null);
            add(scrollPane, BorderLayout.CENTER);

            updateCards(cardList);
        }

        public void updateCards(List<Card> cardList) {
            cards.keySet().removeIf(id -> {
                boolean willRemove = cardList.stream().noneMatch(c -> c.getId().equals(id));
                if (willRemove) {
                    CardUI cardView = cards.get(id);
                    if (cardView != null)
                        cardView.removeListeners();
                }
                return willRemove;
            });
            cardPanel.removeAll();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weighty = 1.0;

            int hGap = 8;
            for (int i = 0; i < cardList.size(); i++) {
                Card card = cardList.get(i);
                CardUI cardView = cards.get(card.getId());
                if (cardView == null) {
                    cardView = new CardUI(card, this::handleCardSelection);
                    cards.put(card.getId(), cardView);
                }
                gbc.gridx = i;
                gbc.insets = new Insets(0, i == 0 ? 0 : hGap, 0, 0); // left padding except for first card
                cardPanel.add(cardView, gbc);
            }

            cardPanel.revalidate();
            cardPanel.repaint();
        }

        private void handleCardSelection(Card card) {
            System.out.println("Selected card: " + card.getId());
            if (onCardSelect != null)
                onCardSelect.accept(card);
        }
    }

    // PlayView (Main Panel)
    public static class PlayView extends JPanel {
        private final JPanel buttonPanel = new JPanel();
        private final GridUI GridUI;
        private final HandUI HandUI;
        private Point selectedCell;
        private Card selectedCard;

        public PlayView(List<Card> mockCards) {
            this.setLayout(new BorderLayout());
            buttonPanel.setLayout(new BorderLayout());
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setResizeWeight(0.6);
            splitPane.setDividerLocation(0.6);
            splitPane.setOneTouchExpandable(false);
            splitPane.setEnabled(false);

            GridUI = new GridUI(5, 5, this::handleCellClick);
            splitPane.setTopComponent(GridUI);

            HandUI = new HandUI(mockCards, this::handleCardSelection);
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
                if (selectedCard.requiresCoordinates() && selectedCell != null) {
                    String message = String.format("Use card '%s' at cell (%d, %d)?",
                            selectedCard.getName(), selectedCell.x, selectedCell.y);
                    if (confirmSelection(message)) {
                        System.out.printf("[Sandbox] Use card: %s at (%d, %d)\n",
                                selectedCard.getName(), selectedCell.x, selectedCell.y);
                        resetSelections();
                    }
                } else if (!selectedCard.requiresCoordinates()) {
                    String message = String.format("Use card '%s'?", selectedCard.getName());
                    if (confirmSelection(message)) {
                        System.out.printf("[Sandbox] Use card: %s (no coords)\n", selectedCard.getName());
                        resetSelections();
                    }
                }
            } else if (selectedCell != null) {
                String message = String.format("Cast lure at cell (%d, %d)?", selectedCell.x, selectedCell.y);
                if (confirmSelection(message)) {
                    System.out.printf("[Sandbox] Cast at (%d, %d)\n", selectedCell.x, selectedCell.y);
                    resetSelections();
                }
            }
        }

        private void resetSelections() {
            selectedCard = null;
            selectedCell = null;
        }
    }

    // MAIN
    public static void main(String[] args) {
        // Mock cards for the sandbox hand
        List<Card> mockCards = Arrays.asList(
                new Card("card1", CardType.LONG_TERM_PROBABILITY, 2.5f, "Increase probability long term."),
                new Card("card2", CardType.TEMPORARY_PROBABILITY, 1.0f, "Temporary buff for this turn."),
                new Card("card3", CardType.CATCH_MULTIPLIER, 3.0f, "Boost catch amount for next action!"),
                new Card("card4", CardType.FISHING_ATTEMPTS, 5.0f, "Get more fishing attempts this round."),
                new Card("card5", CardType.FISHING_ATTEMPTS, 5.0f, "Get more fishing attempts this round."));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("NDFF PlayView Sandbox");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 400);
            PlayView playView = new PlayView(mockCards);
            frame.setContentPane(playView);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
