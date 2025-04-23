package Equals6.Client.Views;

import java.awt.GridLayout;
import java.awt.Point;
import java.util.function.Consumer;

import javax.swing.JPanel;

import Equals6.Client.Interfaces.IBoardEvents;
import Equals6.Common.LoggerUtil;
import Equals6.Client.Client;

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
    }

    @Override
    public void onReceiveCell(int row, int column, int value) {
        LoggerUtil.INSTANCE.info("Updating cell at (" + row + "," + column + ") with value: " + value);
        for (int i = 0; i < this.getComponentCount(); i++) {
            CellButton cell = (CellButton) this.getComponent(i);
            if (cell.getRow() == row && cell.getCol() == column) {
                cell.setText(value + "");
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
