package Equals6.Common;

public class Cell {
    int x, y;
    int value;

    public Cell(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    protected void addValue(int value) {
        // Ensure the value overflows to a range of 0-3
        this.value = (this.value + value) % 4;
        if (this.value < 0) {
            this.value += 4; // Handle negative values to keep within 0-3
        }
    }

    @Override
    public String toString() {
        return String.format("Cell(%d,%d) = %d", x, y, value);
    }
}
