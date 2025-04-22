package Equals6.Client.Views;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import Equals6.Client.CardView;
import Equals6.Client.Interfaces.ICardControls;

public class Menu extends JMenuBar {
    @SuppressWarnings("unused")
    public Menu(ICardControls controls) {
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Search");
        roomsSearch.addActionListener((event) -> {
            controls.show(CardView.ROOMS.name());
        });
        roomsMenu.add(roomsSearch);
        this.add(roomsMenu);
    }
}