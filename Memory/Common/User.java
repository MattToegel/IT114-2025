package Memory.Common;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long clientId = Constants.DEFAULT_CLIENT_ID;
    private String clientName;
    private boolean isReady = false;
    private boolean tookTurn = false;
    private List<Coord> selections = new ArrayList<>();
    private int points;

    /**
     * Server-side points
     * 
     * @param p
     */
    public void changePoints(int p) {
        this.points += p;
        this.points = Math.max(0, this.points);
    }

    public int getPoints() {
        return points;
    }

    /**
     * Client-side points
     * 
     * @param points
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * @return the selections
     */
    public List<Coord> getSelections() {
        return selections;
    }

    /**
     * Toggles a selection; returns true for add and false for remove
     * 
     * @param selection
     * @return
     */
    public boolean toggleSelection(Coord selection) {
        Coord existing = this.selections.stream()
                .filter(c -> c.getX() == selection.getX() && c.getY() == selection.getY()).findFirst()
                .orElse(null);
        if (existing != null) {
            this.selections.remove(existing);
            return false;
        }
        this.selections.add(selection);
        return true;
    }

    public int getSelectionCount() {
        return this.selections.size();
    }

    /**
     * @param selections the selections to set
     */
    public void setSelections(List<Coord> selections) {
        if (selections == null) {
            this.selections.clear();
            return;
        }
        this.selections = selections;
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
