package NDFF.Client.Views;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import NDFF.Common.Card;

public class CardUI extends JButton {
    public static final int CARD_WIDTH = 120;
    public static final int CARD_HEIGHT = 168;

    public CardUI(Card card, Consumer<Card> onSelect) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        setToolTipText(card.getDescription());
        setText(String.format(
                "<html><div style='width:100%%; white-space:normal;'><ul style='margin:0;padding:0;list-style:none;'>"
                        + "<li><b>Type:</b> %s</li>"
                        + "<li><b>Value:</b> %.2f</li>"
                        + "<li><b>Description:</b> %s</li>"
                        + "<li><b>Requires Coordinate:</b> %s</li>"
                        + "</ul></div></html>",

                card.getName(),
                card.getValue(),
                card.getDescription(),
                card.requiresCoordinates() ? "Yes" : "No"));
        addActionListener(_ -> {
            if (onSelect != null) {
                onSelect.accept(card);
            }
        });
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setAlignmentY(TOP_ALIGNMENT);
    }

    public void removeListeners() {
        // remove all action listeners
        for (ActionListener al : getActionListeners()) {
            removeActionListener(al);
        }
    }
}