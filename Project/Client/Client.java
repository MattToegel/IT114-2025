package Project.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Project.Common.PointsPayload;
import Project.Client.Interfaces.IClientEvents;
import Project.Client.Interfaces.IConnectionEvents;
import Project.Client.Interfaces.IMessageEvents;
import Project.Client.Interfaces.IPhaseEvent;
import Project.Client.Interfaces.IPointsEvent;
import Project.Client.Interfaces.IReadyEvent;
import Project.Client.Interfaces.IRoomEvents;
import Project.Client.Interfaces.ITimeEvents;
import Project.Client.Interfaces.ITurnEvent;
import Project.Common.Command;
import Project.Common.ConnectionPayload;
import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.Phase;
import Project.Common.ReadyPayload;
import Project.Common.RoomAction;
import Project.Common.RoomResultPayload;
import Project.Common.TextFX;
import Project.Common.User;
import Project.Common.TextFX.Color;
import Project.Common.TimerPayload;

/**
 * Demoing bi-directional communication between client and server in a
 * multi-client scenario
 */
public enum Client {
    INSTANCE;

    private Socket server = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    final Pattern ipAddressPattern = Pattern
            .compile("/connect\\s+(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{3,5})");
    final Pattern localhostPattern = Pattern.compile("/connect\\s+(localhost:\\d{3,5})");
    private volatile boolean isRunning = true; // volatile for thread-safe visibility
    private final ConcurrentHashMap<Long, User> knownClients = new ConcurrentHashMap<Long, User>();
    private User myUser = new User();
    private Phase currentPhase = Phase.READY;

    // callback that updates the UI
    private static List<IClientEvents> events = new ArrayList<IClientEvents>();

    public void addCallback(IClientEvents e) {
        events.add(e);
    }

    private void error(String message) {
        LoggerUtil.INSTANCE.severe(TextFX.colorize(String.format("%s", message), Color.RED));
    }

    // needs to be private now that the enum logic is handling this
    private Client() {
        LoggerUtil.INSTANCE.info("Client Created");
    }

    public boolean isConnected() {
        if (server == null) {
            return false;
        }
        // https://stackoverflow.com/a/10241044
        // Note: these check the client's end of the socket connect; therefore they
        // don't really help determine if the server had a problem
        // and is just for lesson's sake
        return server.isConnected() && !server.isClosed() && !server.isInputShutdown() && !server.isOutputShutdown();
    }

    /**
     * Takes an IP address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @return true if connection was successful
     */
    @Deprecated
    private boolean connect(String address, int port) {
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            LoggerUtil.INSTANCE.info("Client connected");
            // Use CompletableFuture to run listenToServer() in a separate thread
            CompletableFuture.runAsync(this::listenToServer);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    /**
     * Takes an ip address and a port to attempt a socket connection to a server.
     * 
     * @param address
     * @param port
     * @param username
     * @param callback (for triggering UI events)
     * @return true if connection was successful
     */
    public boolean connect(String address, int port, String username, IClientEvents callback) {
        myUser.setClientName(username);
        addCallback(callback);
        try {
            server = new Socket(address, port);
            // channel to send to server
            out = new ObjectOutputStream(server.getOutputStream());
            // channel to listen to server
            in = new ObjectInputStream(server.getInputStream());
            LoggerUtil.INSTANCE.info("Client connected");
            // Use CompletableFuture to run listenToServer() in a separate thread
            CompletableFuture.runAsync(this::listenToServer);
            sendClientName(myUser.getClientName());// sync follow-up data (handshake)
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected();
    }

    public long getMyClientId() {
        return myUser.getClientId();
    }

    public void clientSideGameEvent(String str) {
        events.forEach(event -> {
            if (event instanceof IMessageEvents) {
                // Note: using -2 to target GameEventPanel
                ((IMessageEvents) event).onMessageReceive(Constants.GAME_EVENT_CHANNEL, str);
            }
        });
    }

    /**
     * Returns the ClientName of a specific Client by ID.
     * 
     * @param id
     * @return the name, or Room if id is -1, or [Unknown] if failed to find
     */
    @Deprecated
    public String getClientNameFromId(long id) {
        if (id == Constants.DEFAULT_CLIENT_ID) {
            return "Room";
        }
        if (knownClients.containsKey(id)) {
            return knownClients.get(id).getClientName();
        }
        return "[Unknown]";
    }

    public String getDisplayNameFromId(long id) {
        if (id == Constants.DEFAULT_CLIENT_ID) {
            return "Room";
        }
        if (knownClients.containsKey(id)) {
            return knownClients.get(id).getDisplayName();
        }
        return "[Unknown]";
    }

    /**
     * <p>
     * Check if the string contains the <i>connect</i> command
     * followed by an IP address and port or localhost and port.
     * </p>
     * <p>
     * Example format: 123.123.123.123:3000
     * </p>
     * <p>
     * Example format: localhost:3000
     * </p>
     * https://www.w3schools.com/java/java_regex.asp
     * 
     * @param text
     * @return true if the text is a valid connection command
     */
    private boolean isConnection(String text) {
        Matcher ipMatcher = ipAddressPattern.matcher(text);
        Matcher localhostMatcher = localhostPattern.matcher(text);
        return ipMatcher.matches() || localhostMatcher.matches();
    }

    /**
     * Controller for handling various text commands.
     * <p>
     * Add more here as needed
     * </p>
     * 
     * @param text
     * @return true if the text was a command or triggered a command
     * @throws IOException
     */
    private boolean processClientCommand(String text) throws IOException {
        boolean wasCommand = false;
        if (text.startsWith(Constants.COMMAND_TRIGGER)) {
            text = text.substring(1); // remove the /
            // System.out.println("Checking command: " + text);
            if (isConnection("/" + text)) {
                if (myUser.getClientName() == null || myUser.getClientName().isEmpty()) {
                    LoggerUtil.INSTANCE.warning(
                            TextFX.colorize("Please set your name via /name <name> before connecting", Color.RED));
                    return true;
                }
                // replaces multiple spaces with a single space
                // splits on the space after connect (gives us host and port)
                // splits on : to get host as index 0 and port as index 1
                String[] parts = text.trim().replaceAll(" +", " ").split(" ")[1].split(":");
                connect(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                sendClientName(myUser.getClientName());// sync follow-up data (handshake)
                wasCommand = true;
            } else if (text.startsWith(Command.NAME.command)) {
                text = text.replace(Command.NAME.command, "").trim();
                if (text == null || text.length() == 0) {
                    LoggerUtil.INSTANCE
                            .warning(TextFX.colorize("This command requires a name as an argument", Color.RED));
                    return true;
                }
                myUser.setClientName(text);// temporary until we get a response from the server
                LoggerUtil.INSTANCE.info(TextFX.colorize(String.format("Name set to %s", myUser.getClientName()),
                        Color.YELLOW));
                wasCommand = true;
            } else if (text.equalsIgnoreCase(Command.LIST_USERS.command)) {
                String message = TextFX.colorize("Known clients:\n", Color.CYAN);
                LoggerUtil.INSTANCE.info(TextFX.colorize("Known clients:", Color.CYAN));
                message += String.join("\n", knownClients.values().stream()
                        .map(c -> String.format("%s %s %s %s %s pts",
                                c.getDisplayName(),
                                c.getClientId() == myUser.getClientId() ? " (you)" : "",
                                c.isReady() ? "[x]" : "[ ]",
                                c.didTakeTurn() ? "[T]"
                                        : "[ ]",
                                c.getPoints()))
                        .toList());
                LoggerUtil.INSTANCE.info(message);
                wasCommand = true;
            } else if (Command.QUIT.command.equalsIgnoreCase(text)) {
                if (isConnected()) {
                    sendDisconnect();
                }
                close();
                wasCommand = true;
            } else if (Command.DISCONNECT.command.equalsIgnoreCase(text)) {
                sendDisconnect();
                wasCommand = true;
            } else if (text.startsWith(Command.REVERSE.command)) {
                text = text.replace(Command.REVERSE.command, "").trim();
                sendReverse(text);
                wasCommand = true;
            } else if (text.startsWith(Command.CREATE_ROOM.command)) {
                text = text.replace(Command.CREATE_ROOM.command, "").trim();
                if (text == null || text.length() == 0) {
                    LoggerUtil.INSTANCE
                            .warning(TextFX.colorize("This command requires a room name as an argument", Color.RED));
                    return true;
                }
                sendRoomAction(text, RoomAction.CREATE);
                wasCommand = true;
            } else if (text.startsWith(Command.JOIN_ROOM.command)) {
                text = text.replace(Command.JOIN_ROOM.command, "").trim();
                if (text == null || text.length() == 0) {
                    LoggerUtil.INSTANCE
                            .warning(TextFX.colorize("This command requires a room name as an argument", Color.RED));
                    return true;
                }
                sendRoomAction(text, RoomAction.JOIN);
                wasCommand = true;
            } else if (text.startsWith(Command.LEAVE_ROOM.command) || text.startsWith("leave")) {
                // Note: Accounts for /leave and /leaveroom variants (or anything beginning with
                // /leave)
                sendRoomAction(text, RoomAction.LEAVE);
                wasCommand = true;
            } else if (text.startsWith(Command.LIST_ROOMS.command)) {
                text = text.replace(Command.LIST_ROOMS.command, "").trim();

                sendRoomAction(text, RoomAction.LIST);
                wasCommand = true;
            } else if (text.equalsIgnoreCase(Command.READY.command)) {
                sendReady();
                wasCommand = true;
            } else if (text.startsWith(Command.DO_SOMETHING.command)) {
                text = text.replace(Command.DO_SOMETHING.command, "").trim();

                sendDoTurn(text);
                wasCommand = true;
            }
        }
        return wasCommand;
    }

    // Start Send*() methods
    public void sendDoTurn(String text) throws IOException {
        // NOTE for now using ReadyPayload as it has the necessary properties
        // An actual turn may include other data for your project
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.TURN);
        rp.setReady(true); // <- techically not needed as we'll use the payload type as a trigger
        rp.setMessage(text);
        sendToServer(rp);
    }

    /**
     * Sends the client's intent to be ready.
     * Can also be used to toggle the ready state if coded on the server-side
     * 
     * @throws IOException
     */
    public void sendReady() throws IOException {
        ReadyPayload rp = new ReadyPayload();
        rp.setReady(true); // <- techically not needed as we'll use the payload type as a trigger
        sendToServer(rp);
    }

    /**
     * Sends a room action to the server
     * 
     * @param roomName
     * @param roomAction (join, leave, create)
     * @throws IOException
     */
    public void sendRoomAction(String roomName, RoomAction roomAction) throws IOException {
        Payload payload = new Payload();
        payload.setMessage(roomName);
        switch (roomAction) {
            case RoomAction.CREATE:
                payload.setPayloadType(PayloadType.ROOM_CREATE);
                break;
            case RoomAction.JOIN:
                payload.setPayloadType(PayloadType.ROOM_JOIN);
                break;
            case RoomAction.LEAVE:
                payload.setPayloadType(PayloadType.ROOM_LEAVE);
                break;
            case RoomAction.LIST:
                payload.setPayloadType(PayloadType.ROOM_LIST);
                break;
            default:
                LoggerUtil.INSTANCE.warning(TextFX.colorize("Invalid room action", Color.RED));
                break;
        }
        sendToServer(payload);
    }

    /**
     * Sends a reverse message action to the server
     * 
     * @param message
     * @throws IOException
     */
    private void sendReverse(String message) throws IOException {
        Payload payload = new Payload();
        payload.setMessage(message);
        payload.setPayloadType(PayloadType.REVERSE);
        sendToServer(payload);

    }

    /**
     * Sends a disconnect action to the server
     * 
     * @throws IOException
     */
    void sendDisconnect() throws IOException {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.DISCONNECT);
        sendToServer(payload);
    }

    /**
     * Sends a message to the server
     * 
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        // added in Milestone 3 to persist usage of slash commands
        if (processClientCommand(message)) {
            return; // if the message was a command, don't send it to the server
        }
        Payload payload = new Payload();
        payload.setMessage(message);
        payload.setPayloadType(PayloadType.MESSAGE);
        sendToServer(payload);
    }

    /**
     * Sends the client's name to the server (what the user desires to be called)
     * 
     * @param name
     * @throws IOException
     */
    private void sendClientName(String name) throws IOException {
        if (myUser.getClientName() == null || myUser.getClientName().length() == 0) {
            System.out.println(TextFX.colorize("Name must be set first via /name command", Color.RED));
            return;
        }
        ConnectionPayload payload = new ConnectionPayload();
        payload.setClientName(name);
        payload.setPayloadType(PayloadType.CLIENT_CONNECT);
        sendToServer(payload);
    }

    private void sendToServer(Payload payload) throws IOException {
        if (isConnected()) {
            out.writeObject(payload);
            out.flush(); // good practice to ensure data is written out immediately
        } else {
            LoggerUtil.INSTANCE.warning(
                    "Not connected to server (hint: type `/connect host:port` without the quotes and replace host/port with the necessary info)");
        }
    }
    // End Send*() methods

    public void start() throws IOException {
        LoggerUtil.INSTANCE.info("Client starting");

        // Use CompletableFuture to run listenToInput() in a separate thread
        CompletableFuture<Void> inputFuture = CompletableFuture.runAsync(this::listenToInput);

        // Wait for inputFuture to complete to ensure proper termination
        inputFuture.join();
    }

    /**
     * Listens for messages from the server
     */
    private void listenToServer() {
        try {
            while (isRunning && isConnected()) {
                Payload fromServer = (Payload) in.readObject(); // blocking read
                if (fromServer != null) {
                    processPayload(fromServer);

                } else {
                    LoggerUtil.INSTANCE.info("Server disconnected");
                    break;
                }
            }
        } catch (ClassCastException | ClassNotFoundException cce) {
            LoggerUtil.INSTANCE.severe("Error reading object as specified type:", cce);
            // cce.printStackTrace();
        } catch (IOException e) {
            if (isRunning) {
                LoggerUtil.INSTANCE.warning("Connection dropped");
                e.printStackTrace();
            }
        } finally {
            closeServerConnection();
        }
        LoggerUtil.INSTANCE.info("listenToServer thread stopped");
    }

    private void processPayload(Payload payload) {
        LoggerUtil.INSTANCE.info("Received from server: " + payload.toString());
        switch (payload.getPayloadType()) {
            case CLIENT_CONNECT:// unused
                break;
            case CLIENT_ID:
                processClientData(payload);
                break;
            case DISCONNECT:
                processDisconnect(payload);
                break;
            case MESSAGE:
                processMessage(payload);
                break;
            case REVERSE:
                processReverse(payload);
                break;
            case ROOM_CREATE: // unused
                break;
            case ROOM_JOIN:
                processRoomAction(payload);
                break;
            case ROOM_LEAVE:
                processRoomAction(payload);
                break;
            case SYNC_CLIENT:
                processRoomAction(payload);
                break;
            case ROOM_LIST:
                processRoomsList(payload);
                break;
            case PayloadType.READY:
                processReadyStatus(payload, false);
                break;
            case PayloadType.SYNC_READY:
                processReadyStatus(payload, true);
                break;
            case PayloadType.RESET_READY:
                // note no data necessary as this is just a trigger
                processResetReady();
                break;
            case PayloadType.PHASE:
                processPhase(payload);
                break;
            case PayloadType.TURN:
            case PayloadType.SYNC_TURN:
                processTurn(payload);
                break;
            case PayloadType.RESET_TURN:
                // note no data necessary as this is just a trigger
                processResetTurn();
                break;
            case PayloadType.TIME:
                processCurrentTimer(payload);
                break;
            case PayloadType.POINTS:
                processPoints(payload);
                break;
            default:
                LoggerUtil.INSTANCE.warning(TextFX.colorize("Unhandled payload type", Color.YELLOW));
                break;

        }
    }

    // Start process*() methods
    private void processPoints(Payload payload) {
        if (!(payload instanceof PointsPayload)) {
            error("Invalid payload subclass for processCardAdd");
            return;
        }
        PointsPayload pp = (PointsPayload) payload;
        long targetId = pp.getClientId();
        int points = pp.getPoints();
        if (knownClients.containsKey(targetId)) {
            knownClients.get(targetId).setPoints(points);
            try {
                events.stream().forEach(event -> {
                    if (event instanceof IPointsEvent) {
                        ((IPointsEvent) event).onPointsUpdate(targetId, points);
                    }
                });
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error processing points", e);
            }
        }
    }

    private void processCurrentTimer(Payload payload) {
        if (!(payload instanceof TimerPayload)) {
            error("Invalid payload subclass for processCurrentTimer");
            return;
        }
        TimerPayload timerPayload = (TimerPayload) payload;
        try {
            events.forEach(event -> {
                if (event instanceof ITimeEvents) {
                    ((ITimeEvents) event).onTimerUpdate(timerPayload.getTimerType(), timerPayload.getTime());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing current timer", e);
        }
    }

    private void processResetTurn() {
        knownClients.values().forEach(cp -> cp.setTookTurn(false));
        System.out.println("Turn status reset for everyone");
        try {
            events.forEach(event -> {
                if (event instanceof ITurnEvent) {
                    ((ITurnEvent) event).onTookTurn(Constants.DEFAULT_CLIENT_ID, false);
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing reset turn", e);
        }
    }

    private void processTurn(Payload payload) {
        // Note: For now assuming ReadyPayload (this may be changed later)
        if (!(payload instanceof ReadyPayload)) {
            error("Invalid payload subclass for processTurn");
            return;
        }
        ReadyPayload rp = (ReadyPayload) payload;
        if (!knownClients.containsKey(rp.getClientId())) {
            LoggerUtil.INSTANCE.severe(String.format("Received turn status for client id %s who is not known",
                    rp.getClientId()));
            return;
        }
        User cp = knownClients.get(rp.getClientId());
        cp.setTookTurn(rp.isReady());
        if (payload.getPayloadType() != PayloadType.SYNC_TURN) {
            String message = String.format("%s %s their turn", cp.getDisplayName(),
                    cp.didTakeTurn() ? "took" : "reset");
            LoggerUtil.INSTANCE.info(message);
            events.forEach(event -> {
                if (event instanceof IMessageEvents) {
                    ((IMessageEvents) event).onMessageReceive(Constants.GAME_EVENT_CHANNEL,
                            String.format("%s finished their turn", cp.getDisplayName()));
                }
            });
        }
        try {
            events.forEach(event -> {
                if (event instanceof ITurnEvent) {
                    ((ITurnEvent) event).onTookTurn(cp.getClientId(), cp.didTakeTurn());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing turn", e);
        }
    }

    private void processPhase(Payload payload) {
        currentPhase = Enum.valueOf(Phase.class, payload.getMessage());
        System.out.println(TextFX.colorize("Current phase is " + currentPhase.name(), Color.YELLOW));
        try {
            events.forEach(event -> {
                if (event instanceof IPhaseEvent) {
                    ((IPhaseEvent) event).onReceivePhase(currentPhase);
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing phase", e);
        }
    }

    private void processResetReady() {
        // using this to clear all local state
        knownClients.values().forEach(cp -> {
            cp.setReady(false);
            cp.setPoints(0);
            cp.setTookTurn(false);
        });
        try {
            events.forEach(event -> {
                if (event instanceof IReadyEvent) {
                    ((IReadyEvent) event).onReceiveReady(Constants.DEFAULT_CLIENT_ID, false, true);
                } else if (event instanceof ITurnEvent) {
                    ((ITurnEvent) event).onTookTurn(Constants.DEFAULT_CLIENT_ID, false);
                } else if (event instanceof IPointsEvent) {
                    ((IPointsEvent) event).onPointsUpdate(Constants.DEFAULT_CLIENT_ID, -1);
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing reset ready", e);
        }
        System.out.println("Ready status reset for everyone");
    }

    private void processReadyStatus(Payload payload, boolean isQuiet) {
        if (!(payload instanceof ReadyPayload)) {
            error("Invalid payload subclass for processRoomsList");
            return;
        }
        ReadyPayload rp = (ReadyPayload) payload;
        if (!knownClients.containsKey(rp.getClientId())) {
            LoggerUtil.INSTANCE.severe(String.format("Received ready status [%s] for client id %s who is not known",
                    rp.isReady() ? "ready" : "not ready", rp.getClientId()));
            return;
        }
        User cp = knownClients.get(rp.getClientId());
        cp.setReady(rp.isReady());
        if (!isQuiet) {
            LoggerUtil.INSTANCE.info(
                    String.format("%s is %s", cp.getDisplayName(),
                            rp.isReady() ? "ready" : "not ready"));
        }
        try {
            events.forEach(event -> {
                if (event instanceof IReadyEvent) {
                    ((IReadyEvent) event).onReceiveReady(cp.getClientId(), cp.isReady(), isQuiet);
                }
            });

        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing ready status", e);
        }
    }

    private void processRoomsList(Payload payload) {
        if (!(payload instanceof RoomResultPayload)) {
            error("Invalid payload subclass for processRoomsList");
            return;
        }
        RoomResultPayload rrp = (RoomResultPayload) payload;
        List<String> rooms = rrp.getRooms();
        try {
            events.forEach(event -> {
                if (event instanceof IRoomEvents) {
                    ((IRoomEvents) event).onReceiveRoomList(rooms, rrp.getMessage());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing room list", e);
        }
        if (rooms == null || rooms.size() == 0) {
            LoggerUtil.INSTANCE.warning(
                    TextFX.colorize("No rooms found matching your query",
                            Color.RED));
            return;
        }
        LoggerUtil.INSTANCE.info(TextFX.colorize("Room Results:", Color.PURPLE));
        LoggerUtil.INSTANCE.info(
                String.join("\n", rooms));
    }

    private void processClientData(Payload payload) {
        if (myUser.getClientId() != Constants.DEFAULT_CLIENT_ID) {
            LoggerUtil.INSTANCE.warning(TextFX.colorize("Client ID already set, this shouldn't happen", Color.YELLOW));

        }
        myUser.setClientId(payload.getClientId());
        myUser.setClientName(((ConnectionPayload) payload).getClientName());// confirmation from Server
        knownClients.put(myUser.getClientId(), myUser);
        LoggerUtil.INSTANCE.info(TextFX.colorize("Connected", Color.GREEN));
        try {
            events.forEach(event -> {
                if (event instanceof IConnectionEvents) {
                    ((IConnectionEvents) event).onReceiveClientId(myUser.getClientId());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing client data", e);
        }
    }

    private void processDisconnect(Payload payload) {
        try {
            events.forEach(event -> {
                if (event instanceof IConnectionEvents) {
                    ((IConnectionEvents) event).onClientDisconnect(payload.getClientId());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing disconnect", e);
        }
        if (payload.getClientId() == myUser.getClientId()) {
            knownClients.clear();
            myUser.reset();
            LoggerUtil.INSTANCE.info(TextFX.colorize("You disconnected", Color.RED));
        } else if (knownClients.containsKey(payload.getClientId())) {
            User disconnectedUser = knownClients.remove(payload.getClientId());
            if (disconnectedUser != null) {
                LoggerUtil.INSTANCE
                        .info(TextFX.colorize(String.format("%s disconnected", disconnectedUser.getDisplayName()),
                                Color.RED));
            }
        }

    }

    private void processRoomAction(Payload payload) {
        if (!(payload instanceof ConnectionPayload)) {
            error("Invalid payload subclass for processRoomAction");
            return;
        }
        ConnectionPayload connectionPayload = (ConnectionPayload) payload;
        // use DEFAULT_CLIENT_ID to clear knownClients (mostly for disconnect and room
        // transitions)
        if (connectionPayload.getClientId() == Constants.DEFAULT_CLIENT_ID) {
            knownClients.clear();
            try {
                events.forEach(event -> {
                    if (event instanceof IRoomEvents) {
                        ((IRoomEvents) event).onRoomAction(
                                Constants.DEFAULT_CLIENT_ID, // reset
                                connectionPayload.getMessage(), // room name
                                false, // is join
                                true);
                    }
                });
            } catch (Exception e) {
                LoggerUtil.INSTANCE.severe("Error processing room reset action", e);
            }
            return;
        }
        switch (connectionPayload.getPayloadType()) {

            case ROOM_LEAVE:
                // remove from map
                if (knownClients.containsKey(connectionPayload.getClientId())) {
                    // inform UI of user leaving (do this first before removal since UI side uses a
                    // lookup method to fetch display name)
                    try {
                        events.forEach(event -> {
                            if (event instanceof IRoomEvents) {
                                ((IRoomEvents) event).onRoomAction(
                                        connectionPayload.getClientId(),
                                        connectionPayload.getMessage(),
                                        false,
                                        false);
                            }
                        });
                    } catch (Exception e) {
                        LoggerUtil.INSTANCE.severe("Error processing room leave action", e);
                    }
                    knownClients.remove(connectionPayload.getClientId());
                }
                if (connectionPayload.getMessage() != null) {
                    LoggerUtil.INSTANCE.info(TextFX.colorize(connectionPayload.getMessage(), Color.YELLOW));
                }

                break;
            case ROOM_JOIN:
                if (connectionPayload.getMessage() != null) {
                    LoggerUtil.INSTANCE.info(TextFX.colorize(connectionPayload.getMessage(), Color.GREEN));
                }

                // cascade to manage knownClients
            case SYNC_CLIENT:
                // add to map
                if (!knownClients.containsKey(connectionPayload.getClientId())) {
                    User user = new User();
                    user.setClientId(connectionPayload.getClientId());
                    user.setClientName(connectionPayload.getClientName());
                    knownClients.put(connectionPayload.getClientId(), user);
                    // inform UI of user joining
                    try {
                        events.forEach(event -> {
                            if (event instanceof IRoomEvents) {
                                ((IRoomEvents) event).onRoomAction(
                                        connectionPayload.getClientId(), // who
                                        connectionPayload.getMessage(), // room name
                                        true, // is join
                                        connectionPayload.getPayloadType() == PayloadType.SYNC_CLIENT // isQuiet if sync
                                );
                            }
                        });
                    } catch (Exception e) {
                        LoggerUtil.INSTANCE.severe("Error processing room join action", e);
                    }
                }
                break;
            default:
                error("Invalid payload type for processRoomAction");
                break;
        }
    }

    private void processMessage(Payload payload) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(payload.getMessage(), Color.BLUE));
        try {
            events.forEach(event -> {
                if (event instanceof IMessageEvents) {
                    ((IMessageEvents) event).onMessageReceive(payload.getClientId(), payload.getMessage());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing message", e);
        }
    }

    private void processReverse(Payload payload) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(payload.getMessage(), Color.PURPLE));
        try {
            events.forEach(event -> {
                if (event instanceof IMessageEvents) {
                    ((IMessageEvents) event).onMessageReceive(payload.getClientId(), payload.getMessage());
                }
            });
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Error processing reverse message", e);
        }
    }
    // End process*() methods

    /**
     * Listens for keyboard input from the user
     */
    private void listenToInput() {
        try (Scanner si = new Scanner(System.in)) {
            LoggerUtil.INSTANCE.info("Waiting for input"); // moved here to avoid console spam
            while (isRunning) { // Run until isRunning is false
                String userInput = si.nextLine();
                if (!processClientCommand(userInput)) {
                    sendMessage(userInput);
                }
            }
        } catch (IOException ioException) {
            LoggerUtil.INSTANCE.severe("Error in listentToInput()", ioException);
            // ioException.printStackTrace();
        }
        LoggerUtil.INSTANCE.info("listenToInput thread stopped");
    }

    /**
     * Closes the client connection and associated resources
     */
    private void close() {
        isRunning = false;
        closeServerConnection();
        LoggerUtil.INSTANCE.info("Client terminated");
        // System.exit(0); // Terminate the application
    }

    /**
     * Closes the server connection and associated resources
     */
    private void closeServerConnection() {
        try {
            if (out != null) {
                LoggerUtil.INSTANCE.info("Closing output stream");
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (in != null) {
                LoggerUtil.INSTANCE.info("Closing input stream");
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (server != null) {
                LoggerUtil.INSTANCE.info("Closing connection");
                server.close();
                LoggerUtil.INSTANCE.info("Closed Socket");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // LoggerUtil.INSTANCE.severe("Socket Error", e);
        }
    }

    public static void main(String[] args) {
        Client client = Client.INSTANCE;
        try {
            client.start();
        } catch (IOException e) {
            System.out.println("Exception from main()");
            e.printStackTrace();
        }
    }
}