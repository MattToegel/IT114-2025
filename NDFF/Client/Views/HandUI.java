package NDFF.Client.Views;

import NDFF.Client.Client;
import NDFF.Client.Interfaces.ICardEvents;
import NDFF.Common.Card;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;

public class HandUI extends JPanel implements ICardEvents {
    private final HashMap<String, CardUI> cards;
    private final Consumer<Card> onCardSelect;
    private final JPanel cardPanel;

    public HandUI(Consumer<Card> onCardSelect) {
        super(new BorderLayout());
        this.onCardSelect = onCardSelect;

        // 1. Change to GridBagLayout for horizontal packing
        cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cards = new HashMap<>();
        JScrollPane scrollPane = new JScrollPane(cardPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        Client.INSTANCE.registerCallback(this);
    }

    public void addCard(Card card) {
        CardUI cardView = new CardUI(card, this::handleCardSelection);
        cards.put(card.getId(), cardView);

        // Add with GridBagConstraints (for spacing)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = cardPanel.getComponentCount();
        gbc.insets = new Insets(0, gbc.gridx == 0 ? 0 : 8, 0, 0); // 8px left gap after first card
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;
        cardPanel.add(cardView, gbc);

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public void updateCards(List<Card> cardList) {
        // Remove CardObjectViews not in the new list
        cards.keySet().removeIf(id -> {
            boolean willRemove = cardList.stream().noneMatch(c -> c.getId().equals(id));
            if (willRemove) {
                CardUI cardView = cards.get(id);
                if (cardView != null) {
                    cardView.removeListeners();
                }
            }
            return willRemove;
        });
        cardPanel.removeAll();

        // 2. Add or update CardObjectViews for each card with spacing
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1.0;

        for (int i = 0; i < cardList.size(); i++) {
            Card card = cardList.get(i);
            CardUI cardView = cards.get(card.getId());
            if (cardView == null) {
                cardView = new CardUI(card, this::handleCardSelection);
                cards.put(card.getId(), cardView);
            }
            gbc.gridx = i;
            gbc.insets = new Insets(0, i == 0 ? 0 : 8, 0, 0); // 8px gap after first card
            cardPanel.add(cardView, gbc);
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void handleCardSelection(Card card) {
        System.out.println("Selected card: " + card.getId());
        if (onCardSelect != null) {
            onCardSelect.accept(card);
        }
    }

    @Override
    public void onCardAdded(Card card) {
    }

    @Override
    public void onCardRemoved(Card card) {
        CardUI cardView = cards.remove(card.getId());
        if (cardView != null) {
            cardView.removeListeners();
            cardPanel.remove(cardView);
            cardPanel.revalidate();
            cardPanel.repaint();
        }
    }

    @Override
    public void onCardsSync(List<Card> cards) {
        updateCards(cards);
    }
}
