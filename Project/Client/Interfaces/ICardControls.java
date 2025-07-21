package Project.Client.Interfaces;

import javax.swing.JPanel;

import Project.Client.CardViewName;

public interface ICardControls {

    /**
     * Calls CardLayout's next()
     */
    void nextView();

    /**
     * Calls CardLayout's previous()
     */
    void previousView();

    /**
     * Calls CardLayout's show()
     * 
     * @param viewName
     */
    void showView(String viewName);

    /**
     * Calls CardLayout's show()
     * 
     * @param viewName
     */
    void showView(CardViewName viewEnum);

    /**
     * Used to have child views/panels register themselves with ClientUI
     * 
     * @param name
     * @param viewPanel
     */
    void registerView(String name, JPanel viewPanel);

    /**
     * Triggers connection logic which bundles data like host, port, username to
     * pass to Client
     */
    void connect();
}
