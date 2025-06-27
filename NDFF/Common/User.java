package NDFF.Common;

import java.util.concurrent.ConcurrentHashMap;

public class User {
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName;
    private boolean isReady = false;
    private boolean tookTurn = false;
    private ConcurrentHashMap<FishType, Integer> fishQuantities = new ConcurrentHashMap<>();

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
        this.isReady = false;
        this.tookTurn = false;
        this.resetFish();
    }

    /**
     * Resets the session state for the user.
     */
    public void resetSession() {
        this.isReady = false;
        this.tookTurn = false;
        this.resetFish();
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
