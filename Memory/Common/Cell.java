package Memory.Common;

public class Cell {
    private int x, y;
    private String value;
    private boolean isSelected = false; // whether or not it's currently selected during a turn
    private boolean isFlipped = false; // whether or not it's shwon
    private boolean isCollected = false; // whether or not a client matched this
    public Cell(int x, int y, String value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    /**
     * @return the isSelected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    /**
     * @return the isFlipped
     */
    public boolean isFlipped() {
        return isFlipped;
    }

    /**
     * @param isFlipped the isFlipped to set
     */
    public void setFlipped(boolean isFlipped) {
        this.isFlipped = isFlipped;
    }

    /**
     * @return the isCollected
     */
    public boolean isCollected() {
        return isCollected;
    }

    /**
     * @param isCollected the isCollected to set
     */
    public void setCollected(boolean isCollected) {
        this.isCollected = isCollected;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
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

    @Override
    public String toString() {
        if(isCollected){
            return " ";
        }
        return isFlipped? value : "#";
    }
}