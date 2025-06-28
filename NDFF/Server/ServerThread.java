package NDFF.Server;

import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import NDFF.Common.Constants;
import NDFF.Common.Card;
import NDFF.Common.CatchData;
import NDFF.Common.FishType;
import NDFF.Common.LoggerUtil;
import NDFF.Common.Phase;
import NDFF.Common.RoomAction;
import NDFF.Common.TextFX;
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
 * A server-side representation of a single client
 */
public class ServerThread extends BaseServerThread {
    private Consumer<ServerThread> onInitializationComplete; // callback to inform when this object is ready
    // server-side only data
    private float catchMultiplier = 0; // multiplier for catch probability
    private int fishingAttempts = 0; // number of fishing attempts made by the client

    protected float getCatchMultiplier() {
        return catchMultiplier;
    }

    protected void adjustCatchMultiplier(float change) {
        this.catchMultiplier += change;
        if (this.catchMultiplier < 0) {
            this.catchMultiplier = 0; // prevent negative multiplier
        }
    }

    protected int getFishingAttempts() {
        return fishingAttempts;
    }

    protected void adjustFishingAttempts(int change) {
        this.fishingAttempts += change;
        if (this.fishingAttempts < 0) {
            this.fishingAttempts = 0; // prevent negative attempts
        }
    }
    protected void resetCatchMultiplier() {
        this.catchMultiplier = 0;
    }
    protected void resetFishingAttempts() {
        this.fishingAttempts = 0;
    }

    /**
     * A wrapper method so we don't need to keep typing out the long/complex sysout
     * line inside
     * 
     * @param message
     */
    protected void info(String message) {
        LoggerUtil.INSTANCE
                .info(TextFX.colorize(String.format("Thread[%s]: %s", this.getClientId(), message), Color.CYAN));
    }

    /**
     * Wraps the Socket connection and takes a Server reference and a callback
     * 
     * @param myClient
     * @param server
     * @param onInitializationComplete method to inform listener that this object is
     *                                 ready
     */
    protected ServerThread(Socket myClient, Consumer<ServerThread> onInitializationComplete) {
        Objects.requireNonNull(myClient, "Client socket cannot be null");
        Objects.requireNonNull(onInitializationComplete, "callback cannot be null");
        info("ServerThread created");
        // get communication channels to single client
        this.client = myClient;
        // this.clientId = this.threadId(); // An id associated with the thread
        // instance, used as a temporary identifier
        this.onInitializationComplete = onInitializationComplete;

    }

    // Start Send*() Methods
    public boolean sendModifyHand(Card card, boolean isAdd) {
        CardsPayload cp = new CardsPayload(List.of(card));
        cp.setPayloadType(isAdd ? PayloadType.CARDS_ADD : PayloadType.CARDS_REMOVE);
        return sendToClient(cp);
    }

    /**
     * Sends the current hand of cards to the client.
     * Doesn't take data as the cards are already stored in the User object.
     * Chose to explicitly create a method for more precise sync control versus
     * adding auto syning in the add/remove methods.
     * 
     * @return
     */
    public boolean sendCurrentHand() {
        CardsPayload cp = new CardsPayload(getCards());
        cp.setPayloadType(PayloadType.CARDS);
        return sendToClient(cp);
    }

    public boolean sendCaughtFishUpdate(long clientId, int x, int y, CatchData caughtFish) {
        FishPayload fp = new FishPayload(x, y, caughtFish);
        fp.setClientId(clientId);
        return sendToClient(fp);
    }

    public boolean sendResetTurnStatus() {
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.RESET_TURN);
        return sendToClient(rp);
    }

    public boolean sendTurnStatus(long clientId, boolean didTakeTurn) {
        return sendTurnStatus(clientId, didTakeTurn, false);
    }

    public boolean sendTurnStatus(long clientId, boolean didTakeTurn, boolean quiet) {
        // NOTE for now using ReadyPayload as it has the necessary properties
        // An actual turn may include other data for your project
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(quiet ? PayloadType.SYNC_TURN : PayloadType.TURN);
        rp.setClientId(clientId);
        rp.setReady(didTakeTurn);
        return sendToClient(rp);
    }

    public boolean sendCurrentPhase(Phase phase) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.PHASE);
        p.setMessage(phase.name());
        return sendToClient(p);
    }

    public boolean sendResetReady() {
        ReadyPayload rp = new ReadyPayload();
        rp.setPayloadType(PayloadType.RESET_READY);
        return sendToClient(rp);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady) {
        return sendReadyStatus(clientId, isReady, false);
    }

    /**
     * Sync ready status of client id
     * 
     * @param clientId who
     * @param isReady  ready or not
     * @param quiet    silently mark ready
     * @return
     */
    public boolean sendReadyStatus(long clientId, boolean isReady, boolean quiet) {
        ReadyPayload rp = new ReadyPayload();
        rp.setClientId(clientId);
        rp.setReady(isReady);
        if (quiet) {
            rp.setPayloadType(PayloadType.SYNC_READY);
        }
        return sendToClient(rp);
    }

    public boolean sendRooms(List<String> rooms) {
        RoomResultPayload rrp = new RoomResultPayload();
        rrp.setRooms(rooms);
        return sendToClient(rrp);
    }

    protected boolean sendDisconnect(long clientId) {
        Payload payload = new Payload();
        payload.setClientId(clientId);
        payload.setPayloadType(PayloadType.DISCONNECT);
        return sendToClient(payload);
    }

    protected boolean sendResetUserList() {
        return sendClientInfo(Constants.DEFAULT_CLIENT_ID, null, RoomAction.JOIN);
    }

    /**
     * Syncs Client Info (id, name, join status) to the client
     * 
     * @param clientId   use -1 for reset/clear
     * @param clientName
     * @param action     RoomAction of Join or Leave
     * @return true for successful send
     */
    protected boolean sendClientInfo(long clientId, String clientName, RoomAction action) {
        return sendClientInfo(clientId, clientName, action, false);
    }

    /**
     * Syncs Client Info (id, name, join status) to the client
     * 
     * @param clientId   use -1 for reset/clear
     * @param clientName
     * @param action     RoomAction of Join or Leave
     * @param isSync     True is used to not show output on the client side (silent
     *                   sync)
     * @return true for successful send
     */
    protected boolean sendClientInfo(long clientId, String clientName, RoomAction action, boolean isSync) {
        ConnectionPayload payload = new ConnectionPayload();
        switch (action) {
            case JOIN:
                payload.setPayloadType(PayloadType.ROOM_JOIN);
                break;
            case LEAVE:
                payload.setPayloadType(PayloadType.ROOM_LEAVE);
                break;
            default:
                break;
        }
        if (isSync) {
            payload.setPayloadType(PayloadType.SYNC_CLIENT);
        }
        payload.setClientId(clientId);
        payload.setClientName(clientName);
        return sendToClient(payload);
    }

    /**
     * Sends this client's id to the client.
     * This will be a successfully connection handshake
     * 
     * @return true for successful send
     */
    protected boolean sendClientId() {
        ConnectionPayload payload = new ConnectionPayload();
        payload.setPayloadType(PayloadType.CLIENT_ID);
        payload.setClientId(getClientId());
        payload.setClientName(getClientName());// Can be used as a Server-side override of username (i.e., profanity
                                               // filter)
        return sendToClient(payload);
    }

    /**
     * Sends a message to the client
     * 
     * @param message
     * @return true for successful send
     */
    protected boolean sendMessage(long clientId, String message) {
        Payload payload = new Payload();
        payload.setPayloadType(PayloadType.MESSAGE);
        payload.setMessage(message);
        payload.setClientId(clientId);
        return sendToClient(payload);
    }

    // End Send*() Methods
    @Override
    protected void processPayload(Payload incoming) {

        switch (incoming.getPayloadType()) {
            case CLIENT_CONNECT:
                setClientName(((ConnectionPayload) incoming).getClientName().trim());

                break;
            case DISCONNECT:
                currentRoom.handleDisconnect(this);
                break;
            case MESSAGE:
                currentRoom.handleMessage(this, incoming.getMessage());
                break;
            case REVERSE:
                currentRoom.handleReverseText(this, incoming.getMessage());
                break;
            case ROOM_CREATE:
                currentRoom.handleCreateRoom(this, incoming.getMessage());
                break;
            case ROOM_JOIN:
                currentRoom.handleJoinRoom(this, incoming.getMessage());
                break;
            case ROOM_LEAVE:
                currentRoom.handleJoinRoom(this, Room.LOBBY);
                break;
            case ROOM_LIST:
                currentRoom.handleListRooms(this, incoming.getMessage());
                break;
            case READY:
                // no data needed as the intent will be used as the trigger
                try {
                    // cast to GameRoom as the subclass will handle all Game logic
                    ((GameRoom) currentRoom).handleReady(this);
                } catch (Exception e) {
                    sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
                }
                break;
            case TURN:
                // no data needed as the intent will be used as the trigger
                try {
                    // cast to GameRoom as the subclass will handle all Game logic
                    ((GameRoom) currentRoom).handleTurnAction(this, incoming.getMessage());
                } catch (Exception e) {
                    sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do a turn");
                }
                break;
            case CAST:
                try {
                    // cast to GameRoom as the subclass will handle all Game logic
                    CoordPayload cp = (CoordPayload) incoming;
                    ((GameRoom) currentRoom).handleCastAction(this, cp.getX(), cp.getY());
                } catch (Exception e) {
                    sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do a cast");
                }
                break;
            case USE:
                try {
                    CardsPayload cp = (CardsPayload) incoming;
                    // cast to GameRoom as the subclass will handle all Game logic
                    ((GameRoom) currentRoom).handleUseCard(this, cp.getX(), cp.getY(), cp.getCards());
                } catch (Exception e) {
                    sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to use a card");
                }
                break;
            default:
                LoggerUtil.INSTANCE.warning(TextFX.colorize("Unknown payload type received", Color.RED));
                break;
        }
    }

    // limited user data exposer
    protected boolean isReady() {
        return this.user.isReady();
    }

    protected void setReady(boolean isReady) {
        this.user.setReady(isReady);
    }

    protected boolean didTakeTurn() {
        return this.user.didTakeTurn();
    }

    protected void setTookTurn(boolean tookTurn) {
        this.user.setTookTurn(tookTurn);
    }

    protected void addFish(FishType fishType, int quantity) {
        this.user.addFish(fishType, quantity);
    }

    protected int getPoints() {
        return this.user.getPoints();
    }

    protected void addCard(Card card) {
        this.user.addCard(card);
    }

    protected Card removeCard(Card card) {
        return this.user.removeCard(card);
    }

    protected boolean hasCard(Card card) {
        return this.user.hasCard(card);
    }

    protected List<Card> getCards() {
        return this.user.getCards();
    }

    @Override
    protected void onInitialized() {
        // once receiving the desired client name the object is ready
        onInitializationComplete.accept(this);
    }
}
