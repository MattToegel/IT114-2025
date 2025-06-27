package NDFF.Common.Payloads;

import java.util.ArrayList;
import java.util.List;

import NDFF.Common.Card;

public class CardsPayload extends CoordPayload {
    private List<Card> cards = new ArrayList<>();

    public CardsPayload(List<Card> cards) {
        super(-1, -1); // default coordinates
        this.cards = new ArrayList<>(cards); // prevents pass by reference issues
        setPayloadType(PayloadType.CARDS);
    }

    public CardsPayload(int x, int y, List<Card> cards) {
        super(x, y);
        this.cards = new ArrayList<>(cards); // prevents pass by reference issues
        setPayloadType(PayloadType.CARDS);
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards); // return a copy to avoid external modification
    }

    @Override
    public String toString() {
        return super.toString() + " {" +
                "cards=" + cards +
                '}';
    }
}
