package Memory.Common;

public class BooleanCoordPayload extends CoordsPayload {
    private boolean value;

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + "BooleanCoordPayload(" + value + ")";
    }
}
