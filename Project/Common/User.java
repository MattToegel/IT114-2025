package Project.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class User {
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName;
    private boolean isReady = false;
    private boolean tookTurn = false;
    private int points = 0;
    private List<Card> cards = new ArrayList<>();

    /**
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * @param points the points to apply (positive or negative)
     * 
     */
    public void changePoints(int points) {
        this.points += points;
        if (this.points < 0) {
            this.points = 0;
        }
    }

    /**
     * @param points the points to set
     */
    public void setPoints(int points) {
        this.points = points;
    }

    public String handToString() {
        if (cards == null) {
            return String.format("cards=[null]");
        }
        return String.format("cards=[%s]",
                String.join(",", cards.stream().map(Card::toString).toArray(String[]::new)));
    }

    /**
     * @return the cards
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Sets the cards (makes a deep copy to avoid pass-by-ref issues)
     * 
     * @param cards the cards to set
     */
    public void setCards(List<Card> cards) {
        this.cards = cards.stream().map(c -> c.clone()).collect(Collectors.toList());
    }

    public void addCards(List<Card> cards) {
        cards.stream().forEach(c -> this.cards.add(c));
    }

    /**
     * Adds a Card (makes a deep copy to avoid pass-by-ref issues)
     * 
     * @param card
     */
    public void addCard(Card card) {
        this.cards.add(card.clone());
    }

    /**
     * Removed a Card by id (from a Card Reference)
     * 
     * @param card
     */
    public Card removeCard(Card card) {
        // use an identifier rather than reference just in case the reference isn't the
        // same (i.e., cloned object)
        // remove by id and return removed card in a thread safe manner
        synchronized (this.cards) {
            for (Card c : this.cards) {
                if (c.getId() == card.getId()) {
                    this.cards.remove(c);
                    return c;
                }
            }
        }
        return null;
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

    public void reset() {
        this.clientId = Constants.DEFAULT_CLIENT_ID;
        this.clientName = null;
        this.isReady = false;
        this.tookTurn = false;
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
