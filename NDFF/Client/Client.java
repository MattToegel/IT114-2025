package NDFF.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import NDFF.Common.Card;
import NDFF.Common.Cell;
import NDFF.Common.Command;
import NDFF.Common.Constants;
import NDFF.Common.Grid;
import NDFF.Common.LoggerUtil;
import NDFF.Common.Phase;
import NDFF.Common.RoomAction;
import NDFF.Common.TextFX;
import NDFF.Common.User;
import NDFF.Common.Payloads.CardsPayload;
import NDFF.Common.Payloads.ConnectionPayload;
import NDFF.Common.Payloads.CoordPayload;
import NDFF.Common.Payloads.FishPayload;
import NDFF.Common.Payloads.Payload;
import NDFF.Common.Payloads.PayloadType;
import NDFF.Common.Payloads.ReadyPayload;
import NDFF.Common.Payloads.RoomResultPayload;
import NDFF.Common.TextFX.Color;

/**
 * Demoing bi-directional communication between client and server in a
 * multi-client scenario
 */
public enum Client {
    INSTANCE;

    {

        // statically initialize the client-side LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("client.log");
        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);
    }
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
    private Grid grid = new Grid();

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
                        .map(c -> String.format("%s %s %s %s %s",
                                c.getDisplayName(),
                                c.getClientId() == myUser.getClientId() ? " (you)" : "",
                                c.isReady() ? "[x]" : "[ ]",
                                c.didTakeTurn() ? "[T]" : "[ ]",
                                c.getPoints()))
                        .toList());
                LoggerUtil.INSTANCE.info(message);
                wasCommand = true;
            } else if (Command.QUIT.command.equalsIgnoreCase(text)) {
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
            } else if (text.startsWith(Command.EXAMPLE_TURN.command)) {
                text = text.replace(Command.EXAMPLE_TURN.command, "").trim();

                sendDoTurn(text);
                wasCommand = true;
            } else if (text.startsWith(Command.CAST.command)) {
                // Note: This is just an example command, you can replace it with your own logic
                text = text.replace(Command.CAST.command, "").trim();
                if (text == null || text.length() == 0) {
                    LoggerUtil.INSTANCE
                            .warning(TextFX.colorize("This command requires a cast message as an argument", Color.RED));
                    return true;
                }
                String[] coords = text.split(",");
                if (coords.length != 2) {
                    LoggerUtil.INSTANCE.warning(TextFX.colorize("Usage: /cast <x>,<y>", Color.RED));
                    return true;
                }
                try {
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    // check if coordinates are within bounds
                    if (grid.isValidCoordinate(x, y)) {
                        LoggerUtil.INSTANCE
                                .info(TextFX.colorize(String.format("Casting at (%d, %d)", x, y), Color.GREEN));
                        sendCast(x, y);
                    } else {
                        LoggerUtil.INSTANCE.warning(TextFX.colorize("Coordinates out of bounds", Color.RED));
                    }
                } catch (NumberFormatException e) {
                    LoggerUtil.INSTANCE
                            .warning(TextFX.colorize("Coordinates must be integers. Usage: /cast <x>,<y>", Color.RED));
                    return true;
                }
                wasCommand = true;
            } else if (text.startsWith(Command.USE.command)) {
                // Note: This is just an example command, you can replace it with your own logic
                text = text.replace(Command.USE.command, "").trim();
                // command is `/use <card index> (optional)<x>,<y>`, not all cards require
                // coordinates, determined by card.requiresCoordinates()

                String[] parts = text.split(" ");
                if (parts.length < 1 || parts.length > 3) {
                    LoggerUtil.INSTANCE.warning(
                            TextFX.colorize("Usage: /use <card index> [<x>,<y>]", Color.RED));
                    return true;
                }
                int cardIndex;
                try {
                    cardIndex = Integer.parseInt(parts[0].trim()) - 1;
                } catch (NumberFormatException e) {
                    LoggerUtil.INSTANCE.warning(
                            TextFX.colorize("Card index must be an integer. Usage: /use <card index> [<x>,<y>]",
                                    Color.RED));
                    return true;
                }
                int x = -1, y = -1; // default values for coordinates
                String[] coords = (parts.length >= 2 ? parts[1].trim() : "").split(",");
                try {
                    x = Integer.parseInt(coords[0].trim());
                } catch (Exception e) {
                    // ignore, will be set to -1
                }
                try {
                    y = Integer.parseInt(coords[1].trim());
                } catch (Exception e) {
                    // ignore, will be set to -1
                }
                if (cardIndex < 0 || cardIndex >= myUser.getCards().size()) {
                    LoggerUtil.INSTANCE.warning(
                            TextFX.colorize("Card index out of bounds. Usage: /use <card index> [<x>,<y>]", Color.RED));
                    return true;
                }
                Card card = myUser.getCards().get(cardIndex);
                // Note: we won't assume it works, so we don't modify local state, simply send
                // the data
                // Server will confirm any changes
                if (card.requiresCoordinates()) {
                    // check if coordinates are provided
                    if (grid.isValidCoordinate(x, y)) {
                        // send card
                        sendUseCard(card, x, y);
                    } else {
                        LoggerUtil.INSTANCE.warning(TextFX.colorize("Coordinates out of bounds", Color.RED));
                        return true;
                    }
                } else {
                    // no coordinates needed
                    // send card
                    sendUseCard(card);
                }

                LoggerUtil.INSTANCE.info(TextFX.colorize("Using: " + text, Color.GREEN));
                wasCommand = true;
            } else if (text.equalsIgnoreCase(Command.HAND.command)) {
                showHand();
                wasCommand = true;
            } else {
                LoggerUtil.INSTANCE.warning(TextFX.colorize("Unknown command: " + text, Color.RED));
            }
        }
        return wasCommand;
    }

    // misc
    private void showHand() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Card card : myUser.getCards()) {
            String cardData = String.format("%d - %s - %s \n[%s]", i + 1, card.getType(), card.getValue(),
                    card.getDescription());
            sb.append(cardData).append(System.lineSeparator());
            i++;
        }
        LoggerUtil.INSTANCE.info(TextFX.colorize("Your current hand:" + System.lineSeparator() + sb, Color.GREEN));
    }
    // end misc

    // Start Send*() methods
    private void sendUseCard(Card card, int x, int y) throws IOException {
        if (card == null) {
            LoggerUtil.INSTANCE.warning(TextFX.colorize("Card is null, cannot send", Color.RED));
            return;
        }
        CardsPayload payload = new CardsPayload(x, y, List.of(card));
        payload.setPayloadType(PayloadType.USE);
        sendToServer(payload);
    }

    private void sendUseCard(Card card) throws IOException {
        if (card == null) {
            LoggerUtil.INSTANCE.warning(TextFX.colorize("Card is null, cannot send", Color.RED));
            return;
        }
        CardsPayload payload = new CardsPayload(List.of(card));
        payload.setPayloadType(PayloadType.USE);
        sendToServer(payload);
    }

    private void sendCast(int x, int y) throws IOException {
        CoordPayload cp = new CoordPayload(x, y);
        cp.setPayloadType(PayloadType.CAST);
        sendToServer(cp);
    }

    private void sendDoTurn(String text) throws IOException {
        // NOTE for now using ReadyPayload as it has the necessary properties
        // An actual turn may include other data for your project
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.TURN);
        rp.setReady(true); // <- technically not needed as we'll use the payload type as a trigger
        rp.setMessage(text);
        sendToServer(rp);
    }

    /**
     * Sends the client's intent to be ready.
     * Can also be used to toggle the ready state if coded on the server-side
     * 
     * @throws IOException
     */
    private void sendReady() throws IOException {
        ReadyPayload rp = new ReadyPayload();
        rp.setReady(true); // <- technically not needed as we'll use the payload type as a trigger
        sendToServer(rp);
    }

    /**
     * Sends a room action to the server
     * 
     * @param roomName
     * @param roomAction (join, leave, create)
     * @throws IOException
     */
    private void sendRoomAction(String roomName, RoomAction roomAction) throws IOException {
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
    private void sendDisconnect() throws IOException {
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
    private void sendMessage(String message) throws IOException {
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
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("Unexpected error in listenToServer", e);
        } finally {
            closeServerConnection();
        }
        LoggerUtil.INSTANCE.info("listenToServer thread stopped");
    }

    private void processPayload(Payload payload) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(String.format("Received payload: %s", payload), Color.CYAN));
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
            case PayloadType.FISH:
                processFishResult(payload);
                break;
            case PayloadType.CARDS: // sync hand
                processCardsSync(payload);
                break;
            case PayloadType.CARDS_ADD: // add card to hand
                processCardsChange(payload, true);
                break;
            case PayloadType.CARDS_REMOVE: // remove card from hand
                processCardsChange(payload, false);
                break;
            default:
                LoggerUtil.INSTANCE.warning(TextFX.colorize("Unhandled payload type", Color.YELLOW));
                break;

        }
    }

    // Start process*() methods
    private void processCardsChange(Payload payload, boolean isAdd) {
        if (!(payload instanceof CardsPayload)) {
            error("Invalid payload subclass for processCardsChange");
            return;
        }
        CardsPayload cp = (CardsPayload) payload;

        // Note: This block is if we track cards of other players (not in this case)
        /*
         * if (!knownClients.containsKey(cp.getClientId())) {
         * LoggerUtil.INSTANCE.severe(String.
         * format("Received card change for client id %s who is not known",
         * cp.getClientId()));
         * return;
         * }
         * User cpUser = knownClients.get(cp.getClientId());
         * if (cp.getCards() == null || cp.getCards().isEmpty()) {
         * LoggerUtil.INSTANCE.warning(String.format("No cards to %s for client id %s",
         * isAdd ? "add" : "remove", cp.getClientId()));
         * return;
         * }
         * // loop over cp.getCards() and add/remove one by one
         * for (Card card : cp.getCards()) {
         * if (isAdd) {
         * cpUser.addCard(card);
         * LoggerUtil.INSTANCE.info(String.format("Added card %s to %s's hand", card,
         * cpUser.getDisplayName()));
         * } else {
         * cpUser.removeCard(card);
         * LoggerUtil.INSTANCE.info(String.format("Removed card %s from %s's hand",
         * card, cpUser.getDisplayName()));
         * }
         * }
         */
        for (Card card : cp.getCards()) {
            if (isAdd) {
                myUser.addCard(card);
                LoggerUtil.INSTANCE.info(String.format("Added card %s to your hand", card));
            } else {
                myUser.removeCard(card);
                LoggerUtil.INSTANCE.info(String.format("Removed card %s from your hand", card));
            }
        }
    }

    private void processCardsSync(Payload payload) {
        if (!(payload instanceof CardsPayload)) {
            error("Invalid payload subclass for processCardSync");
            return;
        }
        CardsPayload cp = (CardsPayload) payload;

        // This block is if we track cards of other players (not in this case)
        /*
         * if (!knownClients.containsKey(cp.getClientId())) {
         * LoggerUtil.INSTANCE.severe(String.
         * format("Received card sync for client id %s who is not known",
         * cp.getClientId()));
         * return;
         * }
         * User cpUser = knownClients.get(cp.getClientId());
         * if (cp.getCards() == null || cp.getCards().isEmpty()) {
         * LoggerUtil.INSTANCE.warning(String.format("No cards to sync for client id %s"
         * , cp.getClientId()));
         * return;
         * }
         * cpUser.syncCards(cp.getCards());
         */
        myUser.syncCards(cp.getCards());
        showHand();
    }

    private void processFishResult(Payload payload) {
        if (!(payload instanceof FishPayload)) {
            error("Invalid payload subclass for processFishResult");
            return;
        }
        FishPayload fp = (FishPayload) payload;
        // use -1 clientId for reset
        if (fp.getClientId() == Constants.DEFAULT_CLIENT_ID) {
            // reset all fish counts
            knownClients.values().forEach(cp -> cp.resetFish());
            System.out.println("Fish counts reset for everyone");
            return;
        }
        // handle normal fish result
        if (!knownClients.containsKey(fp.getClientId())) {
            LoggerUtil.INSTANCE.severe(String.format("Received fish result for client id %s who is not known",
                    fp.getClientId()));
            return;
        }
        User cp = knownClients.get(fp.getClientId());
        // null fish type is used as a notification of an empty cell
        if (fp.getFishQuantity() != null && fp.getFishQuantity().getFishType() != null) {
            cp.addFish(fp.getFishQuantity().getFishType(), fp.getFishQuantity().getQuantity());
        }

        // update grid and display
        // NOTE: Client-side shows known catches in the grid vs server-side that shows
        // remaining fish
        Cell cell = grid.getCell(fp.getX(), fp.getY());
        if (cell == null) {
            LoggerUtil.INSTANCE
                    .warning(String.format("Cell at (%d, %d) is null, cannot set fish count", fp.getX(), fp.getY()));
            return;
        }
        if (fp.getFishQuantity() == null || fp.getFishQuantity().getFishType() == null) {
            cell.clearFish();
        } else {
            cell.changeFishCount(fp.getFishQuantity().getFishType(), fp.getFishQuantity().getQuantity());
        }
        LoggerUtil.INSTANCE.info(TextFX.colorize(String.format("Current grid: " + grid), Color.PURPLE));
    }

    private void processResetTurn() {
        knownClients.values().forEach(cp -> cp.setTookTurn(false));
        System.out.println("Ready status turn for everyone");
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
        }

    }

    private void processPhase(Payload payload) {

        currentPhase = Enum.valueOf(Phase.class, payload.getMessage());
        System.out.println(TextFX.colorize("Current phase is " + currentPhase.name(), Color.YELLOW));
        if (currentPhase == Phase.READY) {
            // treat this as a session reset
            knownClients.values().forEach(user -> {
                user.resetSession();
            });
            grid.reset();
        } else if (currentPhase == Phase.IN_PROGRESS) {
            // switched from ready to in-progress, init local grid
            grid.generate(5, 5, false);
        }
    }

    private void processResetReady() {
        knownClients.values().forEach(cp -> cp.setReady(false));
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
            System.out.println(
                    String.format("%s is %s", cp.getDisplayName(),
                            rp.isReady() ? "ready" : "not ready"));
        }
    }

    private void processRoomsList(Payload payload) {
        if (!(payload instanceof RoomResultPayload)) {
            error("Invalid payload subclass for processRoomsList");
            return;
        }
        RoomResultPayload rrp = (RoomResultPayload) payload;
        List<String> rooms = rrp.getRooms();
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
    }

    private void processDisconnect(Payload payload) {
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
            return;
        }
        switch (connectionPayload.getPayloadType()) {

            case ROOM_LEAVE:
                // remove from map
                if (knownClients.containsKey(connectionPayload.getClientId())) {
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
                }
                break;
            default:
                error("Invalid payload type for processRoomAction");
                break;
        }
    }

    private void processMessage(Payload payload) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(payload.getMessage(), Color.BLUE));
    }

    private void processReverse(Payload payload) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(payload.getMessage(), Color.PURPLE));
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
            LoggerUtil.INSTANCE.severe("Error in listenToInput()", ioException);
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