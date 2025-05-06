package Memory.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {
    private Cell[][] cells = new Cell[3][3];
    private Random random = new Random();
    private long seed = -1;
    private int rows = 3;
    private int cols = 3;
    private boolean isServer = false;

    public void setIsServer(boolean isServer) {
        this.isServer = isServer;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        random.setSeed(seed);
    }

    public long getSeed() {
        return seed;
    }

    /**
     * Initializes the board of a specific size with random values between 0 and 3
     * for each cell
     * and sets the seed for the random number generator. If the seed is not set,
     * a random seed will be generated.
     */
    public void initialize(int rows, int cols) {
        if (seed == -1) {
            setSeed(random.nextLong());
        }
        this.rows = rows;
        this.cols = cols;
        int totalCells = rows * cols;
        List<String> symbols = new ArrayList<>();
        char symbol = 'A';
        for (int i = 0; i < totalCells / 2; i++) {
            String ss = String.valueOf(symbol++); // symbol +"";
            symbols.add(ss);
            symbols.add(ss);
        }
        if (totalCells % 2 != 0) {
            symbols.add(String.valueOf(symbol++));
        }
        Collections.shuffle(symbols); // shuffle the symbols
        // initialize a dynamic grid
        int index = 0;
        cells = new Cell[rows][cols];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new Cell(i, j, symbols.get(index++));
            }
        }
    }

    /**
     * Initializes the board with a default size of 3x3
     * See initialize(int rows, int cols) for more details.
     */
    public void initialize() {
        initialize(3, 3);
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void flipDown() {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j].setFlipped(false);
            }
        }
    }

    public String getCellValue(int x, int y) {
        if (!isPointWithinBounds(x, y)) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
        return cells[x][y].getValue();
    }

    /**
     * * Gives 1 point for a match; 0 otherwise
     * 
     * @return
     */
    public int getPoints(List<Coord> selections) {
        int points = 0;
        Cell first = null;
        for (Coord coord : selections) {
            int x = coord.getX();
            int y = coord.getY();
            if (!isPointWithinBounds(x, y)) {
                throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
            }
            if (first == null) {
                first = cells[x][y];
            } else if (first.getValue().equalsIgnoreCase(cells[x][y].getValue())) {
                points++;
            }
        }
        return points;
    }

    public boolean isPointWithinBounds(int x, int y) {
        return x >= 0 && x < rows && y >= 0 && y < cols;
    }

    public boolean isCellCollected(int x, int y) {
        if (!isPointWithinBounds(x, y)) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
        return cells[x][y].isCollected();
    }

    public void setSelected(int x, int y, boolean selected) {
        if (!isPointWithinBounds(x, y)) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
        cells[x][y].setSelected(selected);
    }

    public void setFlipped(int x, int y, boolean flipped) {
        if (!isPointWithinBounds(x, y)) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
        cells[x][y].setFlipped(flipped);
    }

    public void setCollected(int x, int y, boolean collected) {
        if (!isPointWithinBounds(x, y)) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }
        cells[x][y].setCollected(collected);
    }

    /**
     * Returns a string representation of the board as a formatted grid
     */
    @Override
    public String toString() {
        // returns as a formatted grid
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                sb.append(String.format("[%s]",
                        isServer ? cells[i][j].getValue() : cells[i][j]));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Board board = new Board();
        board.initialize(6, 6);
        board.setIsServer(true);
        System.out.println(board.toString());
    }
}
