package Equals6.Client.Views;

import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import Equals6.Client.Client;
import Equals6.Client.Interfaces.ICardEvents;
import Equals6.Common.Card;
import Equals6.Common.LoggerUtil;

public class HandPanel extends JPanel implements ICardEvents {
    private Consumer<Card> cardSelectedCallback;

    public HandPanel(Consumer<Card> cardSelectedCallback) {
        this.cardSelectedCallback = cardSelectedCallback;
        // register with Client to receive events
        Client.INSTANCE.addCallback(this);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    @SuppressWarnings("unused")
    @Override
    public void onReceiveCards(List<Card> cards) {
        if (cards == null) {
            LoggerUtil.INSTANCE.warning("Received null cards list in HandPanel.onReceiveCards()");
            return;
        }
        if (cards.size() > this.getComponentCount()) {
            int diff = cards.size() - this.getComponentCount();
            for (int i = 0; i < diff; i++) {
                CardButton cardButton = new CardButton(null);
                cardButton.setPreferredSize(new Dimension(100, 150));
                cardButton.setMaximumSize(cardButton.getPreferredSize());
                cardButton.addActionListener(event -> {
                    if (cardButton.getCard() != null) {
                        cardSelectedCallback.accept(cardButton.getCard());
                    }
                });
                this.add(cardButton);
            }
        }
        for (int i = 0; i < this.getComponentCount(); i++) {
            CardButton cardButton = (CardButton) this.getComponent(i);

            if (i < Client.INSTANCE.getHandSize()) {
                cardButton.setCard(Client.INSTANCE.getCard(i));
            } else {
                cardButton.setCard(null);

            }
        }
    }

    @Override
    public void onDiscardCard(Card card) {
        for (int i = 0; i < this.getComponentCount(); i++) {
            CardButton cardButton = (CardButton) this.getComponent(i);
            if (cardButton.getCard() != null && cardButton.getCard().getId() == card.getId()) {
                cardButton.setCard(null);
                break;
            }
        }
    }

    @Override
    public void onReset() {
        this.removeAll();
    }

}
