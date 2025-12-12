package Project.Client.Views;

import Project.Client.CardViewName;
import Project.Client.Interfaces.ICardControls;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MenuBar extends JMenuBar {
    public MenuBar(ICardControls controls) {
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Show Panel");
        roomsSearch.addActionListener(e -> {
            controls.showView(CardViewName.ROOMS.name());
        });
        roomsMenu.add(roomsSearch);
        this.add(roomsMenu);
    }
}
