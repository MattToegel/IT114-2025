package Memory.Server;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import Memory.Common.Board;
import Memory.Common.Constants;
import Memory.Common.Coord;
import Memory.Common.LoggerUtil;
import Memory.Common.Phase;
import Memory.Common.TimedEvent;
import Memory.Exceptions.MissingCurrentPlayerException;
import Memory.Exceptions.NotPlayersTurnException;
import Memory.Exceptions.NotReadyException;
import Memory.Exceptions.PhaseMismatchException;
import Memory.Exceptions.PlayerNotFoundException;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private List<ServerThread> turnOrder = new ArrayList<>();
    private long currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
    private int round = 0;
    private Board board = new Board();

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerThread sp) {
        // sync GameRoom state to new client
        new TimedEvent(1, () -> {
            syncCurrentPhase(sp);
            syncReadyStatus(sp);
            syncPlayerPoints(sp);
            if (currentPhase == Phase.IN_PROGRESS) {
                syncTurnStatus(sp);
                syncBoardDimensions(sp);
            }
        });

    }

    /** {@inheritDoc} */
    @Override
    protected void onClientRemoved(ServerThread sp) {
        // added after Summer 2024 Demo
        // Stops the timers so room can clean up
        LoggerUtil.INSTANCE.info("Player Removed, remaining: " + clientsInRoom.size());
        long removedClient = sp.getClientId();
        turnOrder.removeIf(player -> player.getClientId() == sp.getClientId());
        if (clientsInRoom.isEmpty()) {
            resetReadyTimer();
            resetTurnTimer();
            resetRoundTimer();
            onSessionEnd();
        } else if (removedClient == currentTurnClientId) {
            onTurnStart();
        }
    }

    // timer handlers
    private void startRoundTimer() {
        roundTimer = new TimedEvent(30, () -> onRoundEnd());
        roundTimer.setTickCallback((time) -> System.out.println("Round Time: " + time));
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> System.out.println("Turn Time: " + time));
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }
    }
    // end timer handlers

    // lifecycle methods

    /** {@inheritDoc} */
    @Override
    protected void onSessionStart() {
        LoggerUtil.INSTANCE.info("onSessionStart() start");
        changePhase(Phase.IN_PROGRESS);
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        setTurnOrder();
        board.setIsServer(true);
        board.initialize(6, 6);
        LoggerUtil.INSTANCE.info(String.format("Generated Board: \n%s", board));
        sendBoardDimensions();
        round = 0;
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        round++;
        relay(null, String.format("Round %d has started", round));
        resetRoundTimer();
        resetTurnStatus();
        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();

        changePhase(Phase.IN_PROGRESS);
        try {
            ServerThread currentPlayer = getNextPlayer();
            currentPlayer.setTookTurn(false);
            currentPlayer.setSelections(null);
            relay(null, String.format("It's %s's turn", currentPlayer.getDisplayName()));
        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
        }
        startTurnTimer();
        LoggerUtil.INSTANCE.info("onTurnStart() end");
    }

    // Note: logic between Turn Start and Turn End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onTurnEnd() {
        LoggerUtil.INSTANCE.info("onTurnEnd() start");
        resetTurnTimer(); // reset timer if turn ended without the time expiring
        try {
            // check points
            ServerThread current = getCurrentPlayer();
            if (current.getSelectionCount() == 2) {
                int points = board.getPoints(current.getSelections());

                if (points > 0) {
                    current.changePoints(points);
                    sendPlayerPoints(current.getClientId(), points);// forgot to add in MS2 part of video
                    // sendPoints
                    relay(null, String.format("%s received a point", current.getDisplayName()));
                    sendPickedCells(current.getSelections(), true);
                } else {
                    relay(null, String.format("%s received no points", current.getDisplayName()));
                    sendPickedCells(current.getSelections(), false);
                }
            }
            // delay next round
            changePhase(Phase.END_TURN_DELAY);
            new TimedEvent(5, () -> {
                sendFlipDown();
                // optionally can use checkAllTookTurn();
                try {
                    if (isLastPlayer()) {
                        // if the current player is the last player in the turn order, end the round
                        onRoundEnd();
                    } else {
                        onTurnStart();
                    }
                } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

                    e.printStackTrace();
                }
            });

        } catch (MissingCurrentPlayerException | PlayerNotFoundException e) {

            e.printStackTrace();
        }
        LoggerUtil.INSTANCE.info("onTurnEnd() end");
    }

    // Note: logic between Round Start and Round End is typically handled via timers
    // and user interaction
    /** {@inheritDoc} */
    @Override
    protected void onRoundEnd() {
        LoggerUtil.INSTANCE.info("onRoundEnd() start");
        resetRoundTimer(); // reset timer if round ended without the time expiring

        LoggerUtil.INSTANCE.info("onRoundEnd() end");
        if (round >= 3) {
            onSessionEnd();
        } else {
            onRoundStart();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");
        // scoreboard
        StringBuilder sb = new StringBuilder();
        turnOrder.stream().sorted((sp1, sp2) -> {
            return Integer.compare(sp2.getPoints(), sp1.getPoints());
        }).forEach(sp -> {
            sb.append(String.format("%s: %d\n", sp.getDisplayName(), sp.getPoints()));
        });
        relay(null, String.format("Scoreboard:\n%s", sb.toString()));
        turnOrder.clear();
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        resetReadyStatus();
        resetTurnStatus();
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerUser(s)
    public void sendFlipDown() {
        // sync flip down to all clients in room
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendFlipDown();
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    public void sendPickedCells(List<Coord> selections, boolean collected) {
        // sync picked cells to all clients in room
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPickedCells(selections, collected);
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    public void syncPlayerPoints(ServerThread sp) {
        // sync points to incoming client
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !sp.sendPlayerPoints(spInRoom.getClientId(), spInRoom.getPoints());
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    public void sendPlayerPoints(long clientId, int points) {
        // sync points to all clients in room
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPlayerPoints(clientId, points);
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    private void syncSelection(ServerThread sp, int x, int y, boolean selected) {
        boolean failedToSend = !sp.sendSelection(new Coord(x, y), selected);
        if (failedToSend) {
            removeClient(sp);
        }
    }

    private void syncBoardDimensions(ServerThread sp) {
        boolean failedToSend = !sp.sendBoardDimensions(new Coord(board.getRows(), board.getCols()));
        if (failedToSend) {
            removeClient(sp);
        }
    }

    private void sendBoardDimensions() {
        Coord coord = new Coord(board.getRows(), board.getCols());
        clientsInRoom.values().forEach(spInRoom -> {

            boolean failedToSend = !spInRoom.sendBoardDimensions(coord);
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    private void sendResetTurnStatus() {
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendResetTurnStatus();
            if (failedToSend) {
                removeClient(spInRoom);
            }
        });
    }

    private void sendTurnStatus(ServerThread client, boolean tookTurn) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendTurnStatus(client.getClientId(), client.didTakeTurn());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void syncTurnStatus(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendTurnStatus(serverUser.getClientId(),
                        serverUser.didTakeTurn(), true);
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    // end send data to ServerThread(s)

    // misc methods
    private void resetTurnStatus() {
        clientsInRoom.values().forEach(sp -> {
            sp.setTookTurn(false);
        });
        sendResetTurnStatus();
    }

    private void setTurnOrder() {
        turnOrder.clear();
        turnOrder = clientsInRoom.values().stream().filter(ServerThread::isReady).collect(Collectors.toList());
        Collections.shuffle(turnOrder);
    }

    private ServerThread getCurrentPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // quick early exit
        if (currentTurnClientId == Constants.DEFAULT_CLIENT_ID) {
            throw new MissingCurrentPlayerException("Current Plaer not set");
        }
        return turnOrder.stream()
                .filter(sp -> sp.getClientId() == currentTurnClientId)
                .findFirst()
                // this shouldn't occur but is included as a "just in case"
                .orElseThrow(() -> new PlayerNotFoundException("Current player not found in turn order"));
    }

    private ServerThread getNextPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        int index = 0;
        if (currentTurnClientId != Constants.DEFAULT_CLIENT_ID) {
            index = turnOrder.indexOf(getCurrentPlayer()) + 1;
            if (index >= turnOrder.size()) {
                index = 0;
            }
        }
        ServerThread nextPlayer = turnOrder.get(index);
        currentTurnClientId = nextPlayer.getClientId();
        return nextPlayer;
    }

    private boolean isLastPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // check if the current player is the last player in the turn order
        return turnOrder.indexOf(getCurrentPlayer()) == (turnOrder.size() - 1);
    }

    private void checkAllTookTurn() {
        int numReady = clientsInRoom.values().stream()
                .filter(sp -> sp.isReady())
                .toList().size();
        int numTookTurn = clientsInRoom.values().stream()
                // ensure to verify the isReady part since it's against the original list
                .filter(sp -> sp.isReady() && sp.didTakeTurn())
                .toList().size();
        if (numReady == numTookTurn) {
            relay(null,
                    String.format("All players have taken their turn (%d/%d) ending the round", numTookTurn, numReady));
            onRoundEnd();
        }
    }

    // start check methods
    private void checkCurrentPlayer(long clientId) throws NotPlayersTurnException {
        if (currentTurnClientId != clientId) {
            throw new NotPlayersTurnException("You are not the current player");
        }
    }

    // end check methods

    // receive data from ServerThread (GameRoom specific)
    protected void handlePickAction(ServerThread sender, int x, int y) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(sender);
            checkCurrentPhase(sender, Phase.IN_PROGRESS);
            checkCurrentPlayer(sender.getClientId());
            checkIsReady(sender);
            if (sender.didTakeTurn()) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already taken your turn this round");
                return;
            }
            if (!board.isPointWithinBounds(x, y)) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "Invalid coordinates, please try again");
                return;
            }
            if (board.isCellCollected(x, y)) {
                sender.sendMessage(Constants.DEFAULT_CLIENT_ID,
                        "You have already collected this cell, please try again");
                return;
            }
            // record selection on User
            if (sender.toggleSelection(new Coord(x, y))) {
                LoggerUtil.INSTANCE
                        .info(String.format("User %s selected cell (%d, %d)", sender.getDisplayName(), x, y));

                syncSelection(sender, x, y, true);
            } else {

                LoggerUtil.INSTANCE
                        .info(String.format("User %s unselected cell (%d, %d)", sender.getDisplayName(), x, y));
                syncSelection(sender, x, y, false);
            }
            LoggerUtil.INSTANCE.info(String.format("Current Board: \n%s", board));
            if (sender.getSelectionCount() == 2) {
                // if selection count == 2
                sender.setTookTurn(true);

                sendTurnStatus(sender, sender.didTakeTurn());

                onTurnEnd();
            }

        } catch (NotPlayersTurnException e) {
            sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handlePickAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handlePickAction exception", e);
        } catch (PlayerNotFoundException e) {
            sender.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handlePickAction exception", e);
        } catch (PhaseMismatchException e) {
            sender.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only take a turn during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handlePickAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handlePickAction exception", e);
        }
    }

    /**
     * Example turn action
     * 
     * @param currentUser
     */
    protected void handleTurnAction(ServerThread currentUser, String exampleText) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
            if (currentUser.didTakeTurn()) {
                currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You have already taken your turn this round");
                return;
            }
            currentUser.setTookTurn(true);
            // TODO handle example text possibly or other turn related intention from client
            sendTurnStatus(currentUser, currentUser.didTakeTurn());

            onTurnEnd();
        } catch (NotPlayersTurnException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "It's not your turn");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID, "You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendMessage(Constants.DEFAULT_CLIENT_ID,
                    "You can only take a turn during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        }
    }

    // end receive data from ServerThread (GameRoom specific)
}
