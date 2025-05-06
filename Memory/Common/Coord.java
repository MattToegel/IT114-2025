package Memory.Common;

import java.io.Serializable;

public class Coord implements Serializable {
    private int x, y;
    private String value = "";

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("(%s,%s) = %s", x, y, value);
    }
}
