package Project.Server;

import Project.Common.Constants;
import Project.Common.LoggerUtil;
import Project.Common.Phase;
import Project.Common.TimedEvent;
import Project.Exceptions.PhaseMismatchException;
import Project.Exceptions.PlayerNotFoundException;

/**
 * No edits should be needed in this file, this prepares the core logic for the
 * GameRoom
 */
public abstract class BaseGameRoom extends Room {

    private TimedEvent readyTimer = null;

    protected final int MINIMUM_REQUIRED_TO_START = 2;

    protected Phase currentPhase = Phase.READY;

    protected boolean allowToggleReady = false;

    public BaseGameRoom(String name) {
        super(name);
    }

    /**
     * Project session initialization step (triggered from readyCheck)
     */
    protected abstract void onSessionStart();

    /**
     * Round initialization step (in some cases can be used in place of turns if
     * simple enough)
     */
    protected abstract void onRoundStart();

    /**
     * Turn initialization step (if there are distinct turns)
     */
    protected abstract void onTurnStart();

    /**
     * Turn cleanup step
     */
    protected abstract void onTurnEnd();

    /**
     * Round cleanup step
     */
    protected abstract void onRoundEnd();

    /**
     * Session cleanup step
     */
    protected abstract void onSessionEnd();

    /**
     * Triggered when a client is successfully added to the base-Room map and the
     * GameRoom map
     * 
     * @param client the client who joined
     */
    protected abstract void onClientAdded(ServerThread client);

    /**
     * Triggered when a client is removed from the base-Room map and the GameRoom
     * map
     * 
     * @param client the client who was removed (can be null if removal already
     *               occurred)
     */
    protected abstract void onClientRemoved(ServerThread client);

    @Override
    protected synchronized void addClient(ServerThread client) {
        if (!isRunning()) { // block action if Room isn't running
            return;
        }
        // do the base Room class logic
        super.addClient(client);
        onClientAdded(client);
    }

    @Override
    protected synchronized void removeClient(ServerThread client) {
        if (!isRunning()) { // block action if Room isn't running
            return;
        }
        LoggerUtil.INSTANCE.info("Players in room: " + clientsInRoom.size());
        // do the base-class logic
        super.removeClient(client);
        onClientRemoved(client);
    }

    @Override
    protected synchronized void disconnect(ServerThread client) {
        super.disconnect(client);
        LoggerUtil.INSTANCE.info("Players in room: " + clientsInRoom.size());
        onClientRemoved(client);
    }

    /**
     * Cancels any in progress readyTimer
     */
    protected void resetReadyTimer() {
        if (readyTimer != null) {
            readyTimer.cancel();
            readyTimer = null;
        }
    }

    /**
     * Starts the ready timer
     * 
     * @param resetOnTry when true, will cancel any active readyTimer
     */
    protected void startReadyTimer(boolean resetOnTry) {
        if (resetOnTry) {
            resetReadyTimer();
        }
        if (readyTimer == null) {
            readyTimer = new TimedEvent(30, () -> {
                // callback to trigger when ready expires
                checkReadyStatus();
            });
            readyTimer.setTickCallback((time) -> System.out.println("Ready Timer: " + time));
        }
    }

    /**
     * Rules to begin a session: At least MINIMUM_REQUIRED_TO_START must be joined
     * and ready
     */
    private void checkReadyStatus() {
        long numReady = clientsInRoom.values().stream().filter(p -> p.isReady()).count();
        if (numReady >= MINIMUM_REQUIRED_TO_START) {
            resetReadyTimer();
            onSessionStart();
        } else {
            onSessionEnd();
        }
    }

    protected void resetReadyStatus() {
        clientsInRoom.values().forEach(p -> p.setReady(false));
        sendResetReadyTrigger();
    }

    /**
     * Attempts to change the current phase if the passed phase differs.
     * If it changes, sends the update to all Clients
     * 
     * @param phase
     */
    protected void changePhase(Phase phase) {
        if (currentPhase != phase) {
            currentPhase = phase;
            sendCurrentPhase();
        }
    }

    // send/sync data to ServerThread(s)

    /**
     * Syncs the current phase to a single client
     * 
     * @param sp
     */
    protected void syncCurrentPhase(ServerThread sp) {
        sp.sendCurrentPhase(currentPhase);
    }

    /**
     * Sends the current phase to all clients
     */
    protected void sendCurrentPhase() {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendCurrentPhase(currentPhase);
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    /**
     * A shorthand way of telling all clients to reset their local list's ready
     * status
     */
    protected void sendResetReadyTrigger() {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendResetReady();
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    /**
     * Sends the ready status of each ServerThread to one client
     * 
     * @param incomingSP
     */
    protected void syncReadyStatus(ServerThread incomingSP) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !incomingSP.sendReadyStatus(spInRoom.getClientId(), spInRoom.isReady(), true);
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend && spInRoom.getClientId() == incomingSP.getClientId();
        });
    }

    /**
     * Sends the ready status of one ServerThread to all clients
     * 
     * @param incomingSP
     * @param isReady
     */
    protected void sendReadyStatus(ServerThread incomingSP, boolean isReady) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendReadyStatus(incomingSP.getClientId(), incomingSP.isReady());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }
    // end send data to ServerThread(s)

    // receive data from ServerThread (GameRoom specific)
    protected void handleReady(ServerThread sender) {
        try {
            // early exit checks
            checkPlayerInRoom(sender);
            checkCurrentPhase(sender, Phase.READY);

            ServerThread sp = null;
            // option 1: simply just mark ready
            if (!allowToggleReady) {
                sp = clientsInRoom.get(sender.getClientId());
                sp.setReady(true);
            }
            // option 2: toggle
            else {
                sp = clientsInRoom.get(sender.getClientId());
                sp.setReady(!sp.isReady());
            }
            startReadyTimer(false); // <-- triggers the next step when it expires

            sendReadyStatus(sp, sp.isReady());
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleReady exception", e);
        }

    }
    // end receive data from ServerThread (GameRoom specific)

    // Logic Checks

    /**
     * Early exit (via exception throwing) if it's not the proper phase
     * 
     * @param client
     * @param check
     * @throws Exception
     */
    protected void checkCurrentPhase(ServerThread client, Phase check) throws Exception {
        if (currentPhase != check) {
            client.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    String.format("Current phase is %s, please try again later", currentPhase.name()));
            throw new PhaseMismatchException("Invalid Phase");
        }
    }

    /**
     * Early exit (via exception throwing) if the user isn't in the room
     * 
     * @param client
     * @throws Exception
     */
    protected void checkPlayerInRoom(ServerThread client) throws Exception {
        if (!clientsInRoom.containsKey(client.getClientId())) {
            LoggerUtil.INSTANCE.severe("Player isn't in room");
            throw new PlayerNotFoundException("Player isn't in room");
        }
    }
    // end Logic Checks
}