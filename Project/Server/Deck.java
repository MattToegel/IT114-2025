package Project.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Project.Common.Card;

public class Deck {
    private List<Card> deck = new ArrayList<>();

    void initialize() {
        deck.clear();
        // generate 10 of each card +1, +2, +3, -1, -2, -3, 0
        // TODO some projects require loading content from a file (recommended 1 card
        // per line using csv data for each row)
        for (int i = 0; i < 10; i++) {
            deck.add(new Card("+1", 1));
            deck.add(new Card("+2", 2));
            deck.add(new Card("+3", 3));
            deck.add(new Card("-1", -1));
            deck.add(new Card("-2", -2));
            deck.add(new Card("-3", -3));
            deck.add(new Card("0", 0));
        }
        Collections.shuffle(deck);
    }

    List<Card> drawCards(int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) {
                System.out.println("Deck is empty, reinitializing...");
                initialize();
                // return drawCards(count);
            }
            // draw from end to prevent array shift
            int index = deck.size() - 1;
            drawnCards.add(deck.remove(index));
        }
        return drawnCards;
    }

    @Override
    public String toString() {
        return String.format("Deck(%d)", deck.size());
    }
}