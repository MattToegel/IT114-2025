package Equals6.Common;

import java.util.ArrayList;
import java.util.List;

public class CardsPayload extends Payload {
    private List<Card> cards;

    public CardsPayload() {
        setPayloadType(PayloadType.HAND);
    }

    public void setCard(Card card) {
        if (cards == null) {
            cards = new ArrayList<>();
        }
        cards.clear();
        cards.add(card);
    }

    /**
     * @return the cards
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * @param cards the cards to set
     */
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    @Override
    public String toString() {
        if (cards == null) {
            return super.toString() + String.format(" cards=[null]");
        }
        return super.toString() + String.format(" cards=[%s]",
                String.join(",", cards.stream().map(Card::toString).toArray(String[]::new)));
    }
}
