package Memory.Client.Views;

import java.awt.GridLayout;
import java.awt.Point;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Memory.Client.Interfaces.IBoardEvents;
import Memory.Client.Client;
import Memory.Common.LoggerUtil;

public class GridPanel extends JPanel implements IBoardEvents {
    private Consumer<Point> cellSelectedCallback;

    public GridPanel(Consumer<Point> cellSelectedCallback) {
        // Constructor logic if needed
        this.cellSelectedCallback = cellSelectedCallback;
        // register with Client to receive events
        Client.INSTANCE.addCallback(this);
    }

    @Override
    public void onReceiveDimensions(int rows, int columns) {
        this.removeAll();
        LoggerUtil.INSTANCE.info("Generating grid with dimensions: " + rows + "x" + columns);
        if (rows <= 0 || columns <= 0) {
            LoggerUtil.INSTANCE.info("Invalid dimensions received: " + rows + "x" + columns);
            return; // Invalid dimensions, used for reset
        }
        SwingUtilities.invokeLater(() -> {
            this.setLayout(new GridLayout(rows, columns));
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    CellButton cell = new CellButton(i, j, (point) -> {
                        cellSelectedCallback.accept(point);
                        System.out.println("Selected cell: " + point);
                    });
                    this.add(cell);
                }
            }
            invalidate();
            repaint();
        });

    }

    @Override
    public void onReceiveSelection(int row, int column, boolean isSelected) {
        for (int i = 0; i < this.getComponentCount(); i++) {
            CellButton cell = (CellButton) this.getComponent(i);
            if (cell.getRow() == row && cell.getCol() == column) {
                cell.setSelected(isSelected);
                break;
            }
        }
    }

    @Override
    public void onReceiveCell(int row, int column, String value, boolean isFlipped, boolean isCollected) {
        LoggerUtil.INSTANCE.info("Updating cell at (" + row + "," + column + ") with value: " + value + ", flipped: "
                + isFlipped + ", collected: " + isCollected);
        if (row < 0 || column < 0) {
            for (int i = 0; i < this.getComponentCount(); i++) {
                CellButton cell = (CellButton) this.getComponent(i);

                cell.setText(!cell.isEnabled() ? " " : "#");
            }
            return; // Invalid coordinates, used for reset
        }
        /**
         * Grid options:
         * 1. Loop through cells, find which has proper x,y
         * 2. Use Math
         * 2a. int oneDindex = (row * length_of_row) + column; // Indexes
         * 2b. (col) $i % NUMBER_ITEMS_IN_ROW and (row) $i / NUMBER_ITEMS_IN_ROW
         */
        for (int i = 0; i < this.getComponentCount(); i++) {
            CellButton cell = (CellButton) this.getComponent(i);
            if (cell.getRow() == row && cell.getCol() == column) {

                if (isCollected) {
                    cell.setEnabled(false);
                    // continue;
                }
                if (isFlipped) {
                    cell.setText(value + "");
                } else if (cell.isEnabled()) {
                    cell.setText("#");
                } else {
                    cell.setText(" ");
                }
                break;
            }
        }
        invalidate();
        repaint();
    }

    public void resetSelection() {
        cellSelectedCallback.accept(new Point(-1, -1));// Reset to an invalid point
    }

}