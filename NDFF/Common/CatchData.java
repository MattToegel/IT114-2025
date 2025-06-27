package NDFF.Common;

import java.io.Serializable;

public class CatchData implements Serializable {
    private FishType fishType;
    private int quantity;

    public CatchData(FishType fishType, int quantity) {
        this.fishType = fishType;
        this.quantity = quantity;
    }

    public FishType getFishType() {
        return fishType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void changeQuantity(int quantity) {
        if (this.quantity + quantity < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        this.quantity += quantity;
    }

    @Override
    public String toString() {
        return String.format("%s x %d", (fishType != null ? fishType.name() : "null"), quantity);
    }
}
