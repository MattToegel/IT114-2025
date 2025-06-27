package Project.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import Project.Common.LoggerUtil;
import Project.Common.TextFX;
import Project.Common.TextFX.Color;
import Project.Exceptions.DuplicateRoomException;
import Project.Exceptions.RoomNotFoundException;

public enum Server {
    INSTANCE; // Singleton instance

    {
        // statically initialize the server-side LoggerUtil
        LoggerUtil.LoggerConfig config = new LoggerUtil.LoggerConfig();
        config.setFileSizeLimit(2048 * 1024); // 2MB
        config.setFileCount(1);
        config.setLogLocation("server.log");
        // Set the logger configuration
        LoggerUtil.INSTANCE.setConfig(config);
    }
    private int port = 3000;
    // connected clients
    // Use ConcurrentHashMap for thread-safe client management
    // The key is the unique Room name and the Room is the instance
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private boolean isRunning = true;
    private long nextClientId = 0;

    private void info(String message) {
        LoggerUtil.INSTANCE.info(TextFX.colorize(String.format("Server: %s", message), Color.YELLOW));
    }

    private Server() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            info("JVM is shutting down. Perform cleanup tasks.");
            shutdown();
        }));
    }

    /**
     * Gracefully disconnect clients
     */
    private void shutdown() {
        try {
            // chose removeIf over forEach to avoid potential
            // ConcurrentModificationException
            // since empty rooms tell the server to remove themselves
            rooms.values().removeIf(room -> {
                room.disconnectAll();
                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(int port) {
        this.port = port;
        // server listening
        info("Listening on port " + this.port);
        // Simplified client connection loop
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            createRoom(Room.LOBBY);// create the first room (lobby)
            while (isRunning) {
                info("Waiting for next client");
                Socket incomingClient = serverSocket.accept(); // blocking action, waits for a client connection
                info("Client connected");
                // wrap socket in a ServerThread, pass a callback to notify the Server when
                // they're initialized
                ServerThread serverThread = new ServerThread(incomingClient, this::onServerThreadInitialized);
                // start the thread (typically an external entity manages the lifecycle and we
                // don't have the thread start itself)
                serverThread.start();
                // Note: We don't yet add the ServerThread reference to our connectedClients map
            }
        } catch (DuplicateRoomException e) {
            LoggerUtil.INSTANCE.severe(TextFX.colorize("Lobby already exists (this shouldn't happen)", Color.RED));
        } catch (IOException e) {
            LoggerUtil.INSTANCE.severe(TextFX.colorize("Error accepting connection", Color.RED));
            e.printStackTrace();
        } finally {
            info("Closing server socket");
        }
    }

    /**
     * Callback passed to ServerThread to inform Server they're ready to receive
     * data
     * 
     * @param serverThread
     */
    private void onServerThreadInitialized(ServerThread serverThread) {
        // Generate Server controlled clientId
        nextClientId = Math.max(++nextClientId, 1);
        serverThread.setClientId(nextClientId);
        serverThread.sendClientId();// syncs the data to the Client
        // add initialized client to the lobby
        info(String.format("*%s initialized*", serverThread.getDisplayName()));
        try {
            joinRoom(Room.LOBBY, serverThread);
            info(String.format("*%s added to Lobby*", serverThread.getDisplayName()));
        } catch (RoomNotFoundException e) {
            info(String.format("*Error adding %s to Lobby*", serverThread.getDisplayName()));
            e.printStackTrace();
        }
    }

    /**
     * Attempts to create a new Room and add it to the tracked rooms collection
     * 
     * @param name Unique name of the room
     * @return true if it was created and false if it wasn't
     * @throws DuplicateRoomException
     */
    protected void createRoom(String name) throws DuplicateRoomException {
        final String nameCheck = name.toLowerCase();
        if (rooms.containsKey(nameCheck)) {
            throw new DuplicateRoomException(String.format("Room %s already exists", name));
        }
        Room room = Room.LOBBY.equalsIgnoreCase(nameCheck) ? new Room(name) : new GameRoom(name);
        rooms.put(nameCheck, room);
        info(String.format("Created new Room %s", name));
    }

    /**
     * Attempts to move a client (ServerThread) between rooms
     * 
     * @param name   the target room to join
     * @param client the client moving
     * @throws RoomNotFoundException
     * 
     */
    protected void joinRoom(String name, ServerThread client) throws RoomNotFoundException {
        final String nameCheck = name.toLowerCase();
        if (!rooms.containsKey(nameCheck)) {
            throw new RoomNotFoundException(String.format("Room %s wasn't found", name));
        }
        Room currentRoom = client.getCurrentRoom();
        if (currentRoom != null) {
            info("Removing client from previous Room " + currentRoom.getName());
            currentRoom.removeClient(client);
        }
        Room next = rooms.get(nameCheck);
        next.addClient(client);
    }

    /**
     * Lists all rooms that partially match the given String
     * 
     * @param roomQuery
     * @return
     */
    protected List<String> listRooms(String roomQuery) {
        final String nameCheck = roomQuery.toLowerCase();
        return rooms.values().stream()
                .filter(room -> room.getName().toLowerCase().contains(nameCheck))// find partially matched rooms
                .map(room -> room.getName())// map room to String (name)
                .limit(10) // limit to 10 results
                .sorted() // sort the results alphabetically
                .collect(Collectors.toList()); // return a mutable list
    }

    protected void removeRoom(Room room) {
        rooms.remove(room.getName().toLowerCase());
        info(String.format("Removed room %s", room.getName()));
    }

    /**
     * 
     * <p>
     * Note: Not a common use-case; just updated for example sake.
     * </p>
     * Relays the message from the sender to all rooms
     * Adding the synchronized keyword ensures that only one thread can execute
     * these methods at a time,
     * preventing concurrent modification issues and ensuring thread safety
     * 
     * @param message
     * @param sender  ServerThread (client) sending the message or null if it's a
     *                server-generated message
     */
    private synchronized void relayToAllRooms(ServerThread sender, String message) {
        // Note: any desired changes to the message must be done before this line
        String senderString = sender == null ? "Server" : sender.getDisplayName();
        // Note: formattedMessage must be final (or effectively final) since outside
        // scope can't changed inside a callback function (see removeIf() below)
        final String formattedMessage = String.format("%s: %s", senderString, message);
        // end temp identifier

        // loop over Rooms and send out the message
        // Note: this uses a lambda expression for each item in the values() collection

        rooms.values().forEach(room -> {
            room.relay(sender, formattedMessage);
        });
    }

    /**
     * Used to send a message to all Rooms.
     * This is just an example and we likely won't be using this
     * 
     * @param sender
     * @param message
     */
    public synchronized void broadcastMessageToAllRooms(ServerThread sender, String message) {
        relayToAllRooms(sender, message);
    }

    public static void main(String[] args) {
        LoggerUtil.INSTANCE.info("Server Starting");
        Server server = Server.INSTANCE;
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        LoggerUtil.INSTANCE.warning("Server Stopped");
    }

}