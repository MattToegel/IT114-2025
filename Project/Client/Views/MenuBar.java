package Project.Client.Views;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import Project.Client.CardViewName;
import Project.Client.Interfaces.ICardControls;

public class MenuBar extends JMenuBar {
    public MenuBar(ICardControls controls) {
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Show Panel");
        roomsSearch.addActionListener(_ -> {
            controls.showView(CardViewName.ROOMS.name());
        });
        roomsMenu.add(roomsSearch);
        this.add(roomsMenu);
    }
}
