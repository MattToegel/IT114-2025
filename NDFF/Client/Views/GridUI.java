package NDFF.Client.Views;

import NDFF.Client.Client;
import NDFF.Client.Interfaces.IGridEvents;
import NDFF.Common.LoggerUtil;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.util.function.Consumer;

public class GridUI extends JPanel implements IGridEvents {

    private int rows;
    private int cols;
    private CellUI[][] cellViews;
    private final Consumer<Point> onCellClick;
    private JPanel container = new JPanel();

    public GridUI(Consumer<Point> onCellClick) {
        this.onCellClick = onCellClick;
        Client.INSTANCE.registerCallback(this);
        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);
    }

    private void generateGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        // iterate through existing cellViews and remove listeners
        if (cellViews != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    cellViews[r][c].removeAllListeners();
                }
            }
        }
        container.removeAll();
        // generate grid
        cellViews = new CellUI[rows][cols];
        container.setLayout(new GridLayout(rows, cols, 2, 2)); // 2px gap between cells
        Dimension preferredSize = new Dimension(32, 32); // Set preferred size for each cell
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                CellUI cellUI = new CellUI(r, c, this::handleCellClick);
                cellUI.setPreferredSize(preferredSize); // Set preferred size for each cell
                cellViews[r][c] = cellUI;
                container.add(cellUI);
                LoggerUtil.INSTANCE.info("Added cell at (" + r + ", " + c + ")");
            }
        }
        container.revalidate();
        container.repaint();
        this.revalidate();
        this.repaint();
    }

    private void handleCellClick(Point point) {
        // unhighlight all cells
        clearHighlightAll();
        // highlight the clicked cell
        highlightCell(point.x, point.y, Color.BLUE);

        if (onCellClick != null) {
            onCellClick.accept(point);
        }
    }

    private void clearHighlightAll() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                clearHighlight(r, c);
            }
        }
    }

    /**
     * Update the fish count and probability display for a specific cell.
     */
    private void updateCellState(int row, int col, int fishCount) {
        SwingUtilities.invokeLater(() -> {
            // clear highlight for all cells
            clearHighlightAll();
            cellViews[row][col].setFishCount(fishCount);
        });

    }

    /**
     * Highlight a cell (e.g., for selection or action).
     */
    private void highlightCell(int row, int col, Color highlightColor) {
        cellViews[row][col].setHighlight(highlightColor);
    }

    /**
     * Remove highlight from a cell.
     */
    private void clearHighlight(int row, int col) {
        cellViews[row][col].setHighlight(null);
    }

    /**
     * Reset the grid (clear all cell states and highlights).
     */
    private void resetGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cellViews[r][c].setFishCount(-1);
                cellViews[r][c].setHighlight(null);
            }
        }
        container.repaint();
    }

    @Override
    public void onReceiveGridSize(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            resetGrid();
            return;
        }
        generateGrid(rows, cols);
    }

    @Override
    public void onReceiveCellUpdate(int row, int col, int value) {
        updateCellState(row, col, value);
    }
}
