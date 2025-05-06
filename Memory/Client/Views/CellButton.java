package Memory.Client.Views;

import java.awt.Point;
import java.util.function.Consumer;

import javax.swing.JButton;

import Memory.Common.LoggerUtil;

public class CellButton extends JButton {
    private int row, col;

    @SuppressWarnings("unused")
    public CellButton(int row, int col, Consumer<Point> onClick) {
        this.row = row;
        this.col = col;
        setText("#");
        this.addActionListener((event) -> {
            LoggerUtil.INSTANCE.info("CellButton clicked at: " + row + ", " + col);
            onClick.accept(new Point(row, col));
        });
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}