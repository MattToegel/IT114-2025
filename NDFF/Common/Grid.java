package NDFF.Common;

public class Grid {
    private Cell[][] cells;

    /**
     * Generates a grid of cells.
     * Each cell is initialized if isServer is true.
     * 
     * @param rows
     * @param cols
     * @param isServer
     */
    public void generate(int rows, int cols, boolean isServer) {
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(i, j);
                if (isServer) {
                    cells[i][j].initialize();
                }
            }
        }
    }

    public CatchData tryCatchFish(int x, int y) {
        if (!isValidCoordinate(x, y)) {
            throw new IllegalArgumentException("Invalid grid coordinates");
        }
        Cell cell = cells[x][y];
        if (cell == null) {
            throw new IllegalStateException("Cell is not initialized");
        }
        return cell.tryCatchFish();
    }

    public int getHeight() {
        return cells != null ? cells.length : 0;
    }

    public int getWidth() {
        return (cells != null && cells.length > 0) ? cells[0].length : 0;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public boolean hasFish(int row, int col) {
        if (isValidCoordinate(row, col)) {
            Cell cell = cells[row][col];
            return cell != null && cell.getTotalFish() > 0;
        }
        return false;
    }

    public boolean isValidCoordinate(int row, int col) {
        return cells != null && row >= 0 && col >= 0 && row < cells.length && col < cells[0].length;
    }

    /**
     * Resets all cells in the grid by calling their reset method.
     */
    public void reset() {
        if (cells == null)
            return;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                if (cells[i][j] != null) {
                    cells[i][j] = null;
                }
            }
        }
        cells = null; // clear the reference to the grid
    }

    @Override
    public String toString() {

        if (cells == null) {
            return "Grid is not initialized.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                int count = cells[i][j].getTotalFish();
                String cellValue = String.format("[%s]", count > 0 ? count : "X");
                sb.append(cellValue);// .append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
