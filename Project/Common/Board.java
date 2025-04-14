package Project.Common;

import java.util.Random;

public class Board {
    private Cell[][] cells = new Cell[3][3];
    private Random random = new Random();
    private long seed = -1;
    private int rows = 3;
    private int cols = 3;

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
        // initialize a dynamic grid
        cells = new Cell[rows][cols];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new Cell(i, j, random.nextInt(4));// 0-3
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

    /**
     * * Counts all rows and columns that equal 6
     * 
     * @return
     */
    public int getPoints() {
        int points = 0;
        for (int i = 0; i < cells.length; i++) {
            int rowSum = 0;
            for (int j = 0; j < cells[i].length; j++) {
                rowSum += cells[i][j].value;
            }
            if (rowSum == 6) {
                points++;
            }
        }
        for (int j = 0; j < cells[0].length; j++) {
            int colSum = 0;
            for (int i = 0; i < cells.length; i++) {
                colSum += cells[i][j].value;
            }
            if (colSum == 6) {
                points++;
            }
        }
        return points;
    }

    /**
     * * Applies an action to add a value to a cell on the board at the specified
     * coordinates
     * 
     * @param x
     * @param y
     * @param value
     */
    public void applyAction(int x, int y, int value) {
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length) {
            throw new IllegalArgumentException("Invalid cell coordinates");
        }
        cells[x][y].addValue(value);
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
                sb.append(String.format("[%s]", cells[i][j].value));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
