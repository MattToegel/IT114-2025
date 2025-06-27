package NDFF.Common;

public enum FishType {
    COMMON(1),
    UNCOMMON(5),
    RARE(10),
    SUPER_RARE(25),
    LEGENDARY(100);

    private final int pointValue;

    FishType(int pointValue) {
        this.pointValue = pointValue;
    }

    public int getPointValue() {
        return pointValue;
    }
}
