package NDFF.Client.Views;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CellUI extends JPanel {
    private int fishCount = -1; // -1 indicates unknown fish count
    private Color highlightColor = null;
    private JLabel infoLabel;

    public CellUI(int x, int y, Consumer<Point> onClick) {

        setBackground(Color.CYAN);
        setBorder(new LineBorder(Color.DARK_GRAY, 1));
        infoLabel = new JLabel();
        add(infoLabel);
        refresh();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null && isEnabled()) {
                    onClick.accept(new Point(x, y));
                }
            }
        });
    }

    public void removeAllListeners() {
        for (MouseListener ml : getMouseListeners()) {
            removeMouseListener(ml);
        }
    }

    public void setFishCount(int count) {
        this.fishCount = count;
        refresh();
    }

    public void setHighlight(Color color) {
        this.highlightColor = color;
        /*
         * LoggerUtil.INSTANCE.info(TextFX
         * .colorize("CellUI setHighlight: highlightColor=" + color + ", fishCount=" +
         * fishCount,
         * TextFX.Color.RED));
         */
        refresh();
    }

    private void refresh() {
        setEnabled(fishCount > 0 || fishCount == -1); // -1 indicates "unknown", but still clickable
        // log highlight color
        /*
         * LoggerUtil.INSTANCE.info(TextFX
         * .colorize("CellUI refresh: highlightColor=" + highlightColor + ", fishCount="
         * + fishCount,
         * TextFX.Color.RED));
         */
        if (highlightColor != null) {
            setBackground(highlightColor);
        } else {
            setBackground(Color.CYAN);
        }
        if (fishCount == -1) {
            infoLabel.setText(String.format("<html><center>%s</center></html>", "Unknown"));
        } else if (fishCount > 0) {
            infoLabel.setText(String.format("<html><center>Fish: %d</center></html>", fishCount));

        } else {
            infoLabel.setText("<html><center>No Fish</center></html>");
            setBackground(Color.GRAY);
        }
        repaint();
    }
}
