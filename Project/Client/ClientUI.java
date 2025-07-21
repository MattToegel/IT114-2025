package Project.Client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Project.Client.Interfaces.ICardControls;
import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Views.ChatGameView;
import Project.Client.Views.ConnectionView;
import Project.Client.Views.MenuBar;
import Project.Client.Views.RoomsView;
import Project.Client.Views.UserDetailsView;
import Project.Common.Constants;
import Project.Common.LoggerUtil;

public class ClientUI extends JFrame implements ICardControls, IConnectionEvents, IRoomEvents {
    private CardLayout cardLayout = new CardLayout();
    private Container frameContainer;
    private JPanel cardContainer;
    private JPanel activeCardViewPanel;
    private CardViewName activeCardViewEnum;
    private String originalTitle = "";
    private JMenuBar menuBar;
    private JLabel currentRoomLabel = new JLabel(Constants.NOT_CONNECTED);
    // separate UI views
    private ConnectionView connectionView;
    private UserDetailsView userDetailsView;
    private ChatGameView chatGameView;
    private RoomsView roomsView;
    // logger
    {
        // Note: Moved from Client as this file is the entry point now
        // statically initialize the client-side LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("client-ui.log");
        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);
    }

    public static void main(String[] args) {
        // TODO update with your UCID instead of mine
        // Your test or app entry point

        SwingUtilities.invokeLater(() -> {

            try {

                new ClientUI("MT85-Client");

            } catch (Throwable t) {
                LoggerUtil.INSTANCE.severe("Unhandled exception in main thread", t);
            }
        });

    }

    public ClientUI(String title) {
        super(title);
        originalTitle = title;
        setMinimumSize(new Dimension(400, 400));
        setSize(getMinimumSize());
        setLocationRelativeTo(null); // Center the window
        Client.INSTANCE.registerCallback(this);
        // handle window close
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // confirmation dialog to prevent accidental closure
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                int response = JOptionPane.showConfirmDialog(cardContainer,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        Client.INSTANCE.sendDisconnect();
                    } catch (NullPointerException | IOException e) {
                        LoggerUtil.INSTANCE.severe("Error during disconnect: " + e.getMessage());
                    }
                    System.exit(0);
                }
            }
        });

        // setup menu
        menuBar = new MenuBar(this); // "this" contains the interfaces used for callbacks
        this.setJMenuBar(menuBar);

        frameContainer = getContentPane();

        cardContainer = new JPanel();
        cardContainer.setLayout(cardLayout);

        frameContainer.add(currentRoomLabel, BorderLayout.NORTH);
        frameContainer.add(cardContainer, BorderLayout.CENTER);

        // initialize views
        // "this" contains the interfaces used for callbacks
        connectionView = new ConnectionView(this);
        userDetailsView = new UserDetailsView(this);
        chatGameView = new ChatGameView(this);
        roomsView = new RoomsView(this);
        roomsView.setVisible(false);// this line is just to remove the editor warning about unused variable

        pack(); // Resize to fit components
        setVisible(true); // Show the window
    }

    /**
     * Finds the visible/active panel and updates the ClientUI cached references for
     * easier access.
     * Includes a built-in "redirect" if the active panel requires an active
     * connection and the user doesn't have an id set.
     */
    private void findAndSetCurrentView() {
        Component panel = List.of(cardContainer.getComponents()).stream().filter(Component::isVisible)
                .findFirst().orElseThrow();
        if (panel != null) {
            // record active panel and enum reference
            activeCardViewPanel = (JPanel) panel;
            activeCardViewEnum = Enum.valueOf(CardViewName.class, activeCardViewPanel.getName());

            // internal "redirect" if we're not connected
            if (!Client.INSTANCE.isMyClientIdSet() && CardViewName.viewRequiresConnection(activeCardViewEnum)) {
                showView(CardViewName.CONNECT.name());
                setSize(getMinimumSize());
                revalidate();
            }
        }
        LoggerUtil.INSTANCE.fine("Current View: " + activeCardViewPanel.getName());
    }

    // UI interface callbacks start
    @Override
    public void nextView() {
        cardLayout.next(cardContainer);
        findAndSetCurrentView();
    }

    @Override
    public void previousView() {
        cardLayout.previous(cardContainer);
        findAndSetCurrentView();
    }

    @Override
    public void showView(String viewName) {
        cardLayout.show(cardContainer, viewName);
        findAndSetCurrentView();
    }

    @Override
    public void showView(CardViewName viewEnum) {
        showView(viewEnum.name());
    }

    @Override
    public void registerView(String viewName, JPanel panelView) {
        cardContainer.add(panelView, viewName);
    }

    @Override
    public void connect() {
        String username = userDetailsView.getUsername();
        String host = connectionView.getHost();
        int port = connectionView.getPort();
        // update title of Frame for Client identity
        setTitle(String.format("%s - %s", originalTitle, username));

        // trigger connection (NOTE: Differs from the MS2 connect() method)
        Client.INSTANCE.connect(host, port, username);
    }

    // UI interface callbacks end

    // Client interface callbacks start
    @Override
    public void onClientDisconnect(long clientId) {
        if (!CardViewName.viewRequiresConnection(activeCardViewEnum)) {
            LoggerUtil.INSTANCE.warning("Received onClientDisconnect while in a view prior to CHAT");
            return;
        }

        // handle cleanup
        if (Client.INSTANCE.isMyClientId(clientId)) {
            currentRoomLabel.setText(Constants.NOT_CONNECTED);
            showView(CardViewName.CONNECT);
        }
    }

    @Override
    public void onReceiveClientId(long clientId) {
        LoggerUtil.INSTANCE.fine("Received client id: " + clientId);
        showView(CardViewName.CHAT_GAME_SCREEN);// switch to connected view
        chatGameView.showChatOnlyView();
        setSize(new Dimension(600, 600));
        revalidate();
    }

    @Override
    public void onRoomAction(long clientId, String roomName, boolean isJoin, boolean isQuiet) {
        LoggerUtil.INSTANCE.fine(String.format("onRoomAction: clientId=%d, roomName=%s, isJoin=%b, isQuiet=%b",
                clientId, roomName, isJoin, isQuiet));
        if (Client.INSTANCE.isMyClientId(clientId) && isJoin) {
            currentRoomLabel.setText(String.format("Room: %s", roomName));
        }
    }

    @Override
    public void onReceiveRoomList(List<String> rooms, String message) {
        // unused
    }
    // Client interface callbacks end

}
