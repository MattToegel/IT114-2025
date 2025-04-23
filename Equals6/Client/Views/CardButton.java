package Equals6.Client.Views;

import javax.swing.JButton;

import Equals6.Common.Card;

public class CardButton extends JButton {
    private Card myCard;

    public CardButton(Card card) {
        setCard(card);
    }

    public Card getCard() {
        return myCard;
    }

    public void setCard(Card card) {
        this.myCard = card;
        this.setText(card == null ? "" : card.getName()); // Update the button text to the new card name
        this.setVisible(card != null);
        invalidate();
        repaint();
    }
}
