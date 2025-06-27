package NDFF.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName;
    private boolean isReady = false;
    private boolean tookTurn = false;
    private ConcurrentHashMap<FishType, Integer> fishQuantities = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Card> cards = new ConcurrentHashMap<>();

    public void addCard(Card card) {
        if (hasCard(card)) {
            // This may be fine to just ignore
            throw new IllegalArgumentException("Card already exists in user's collection");
        }
        cards.put(card.getId(), card.clone()); // Use put to add or replace, each id is unique
    }

    /**
     * Synchronizes the user's card collection with a new list of cards.
     * This method updates the user's collection to match the provided list,
     * adding new cards and removing those that are not present in the new list.
     * Used client-side when getting updates from the server.
     * 
     * @param newCards
     */
    public void syncCards(List<Card> newCards) {
        if (newCards == null) {
            throw new IllegalArgumentException("Card list cannot be null");
        }
        for (Card card : newCards) {
            if (card == null) {
                throw new IllegalArgumentException("Card cannot be null");
            }
            cards.put(card.getId(), card.clone());
        }
        // Remove cards that are not in newCards
        cards.keySet().removeIf(id -> newCards.stream().noneMatch(c -> c.getId().equals(id)));
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards.values()); // Return a copy of the card list to avoid external modification
    }

    public void clearCards() {
        cards.clear();
    }

    /**
     * Removes a card from the user's collection.
     * 
     * @param card
     * @return removed card
     */
    public Card removeCard(Card card) {
        if (!hasCard(card)) {
            throw new IllegalArgumentException("Card not found in user's collection");
        }
        return cards.remove(card.getId()); // Remove the card by its unique ID
    }

    public boolean hasCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        return cards.containsKey(card.getId()); // Check if the card exists in the user's collection
    }

    public int getPoints() {
        // FishType has a points value
        return fishQuantities.entrySet().stream()
                .mapToInt(entry -> entry.getKey().getPointValue() * entry.getValue())
                .sum();
    }

    public void addFish(FishType fishType, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        fishQuantities.merge(fishType, quantity, Integer::sum);
    }

    public void resetFish() {
        fishQuantities.clear();
    }

    /**
     * @return the clientId
     */
    public long getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the username
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param username the username to set
     */
    public void setClientName(String username) {
        this.clientName = username;
    }

    public String getDisplayName() {
        return String.format("%s#%s", this.clientName, this.clientId);
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    /**
     * Resets the user state, including clientId, clientName, isReady, tookTurn, and
     * fish. All state is cleared to default values.
     */
    public void reset() {
        this.clientId = Constants.DEFAULT_CLIENT_ID;
        this.clientName = null;
        this.resetSession();
    }

    /**
     * Resets the session state for the user.
     */
    public void resetSession() {
        this.isReady = false;
        this.tookTurn = false;
        this.resetFish();
        this.clearCards();
    }

    /**
     * @return the tookTurn
     */
    public boolean didTakeTurn() {
        return tookTurn;
    }

    /**
     * @param tookTurn the tookTurn to set
     */
    public void setTookTurn(boolean tookTurn) {
        this.tookTurn = tookTurn;
    }
}
