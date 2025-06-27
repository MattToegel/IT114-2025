package NDFF.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import NDFF.Common.Card;
import NDFF.Common.CardType;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public Deck() {
        loadCards();
    }

    public void loadCards() {
        // load cards from a csv file from the Assets package of this project

        try (java.io.InputStream is = getClass().getResourceAsStream("../Assets/cards.csv");
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#"))
                    continue; // skip empty or comment lines
                String[] parts = line.split(",");
                String id = parts[0].trim();
                CardType type = CardType.valueOf(parts[1].trim());
                float value = Float.parseFloat(parts[2].trim());
                String description = parts[3].trim();
                int quantity = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 1; // optional quantity
                for (int i = 0; i < quantity; i++) {
                    // create multiple cards if quantity is specified
                    // using UUID to ensure unique IDs (meant for card copies when quantity > 1)
                    cards.add(new Card(id + "-" + UUID.randomUUID(), type, value, description));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load cards: " + e.getMessage());
        }
        shuffle();
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            loadCards(); // reload cards if deck is empty
        }
        return cards.remove((int) (Math.random() * cards.size()));
    }

    public int getTotalCards() {
        return cards.size();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    @Override
    public String toString() {
        return "Deck{" +
                "cards=" + cards +
                '}';
    }
}
