package NDFF.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import NDFF.Common.TextFX;
import NDFF.Common.Constants;
import NDFF.Common.Card;
import NDFF.Common.CatchData;
import NDFF.Common.Grid;
import NDFF.Common.LoggerUtil;
import NDFF.Common.Phase;
import NDFF.Common.TimedEvent;
import NDFF.Common.TimerType;
import NDFF.Common.TextFX.Color;
import NDFF.Exceptions.IsAwayException;
import NDFF.Exceptions.MissingCurrentPlayerException;
import NDFF.Exceptions.NotPlayersTurnException;
import NDFF.Exceptions.NotReadyException;
import NDFF.Exceptions.PhaseMismatchException;
import NDFF.Exceptions.PlayerNotFoundException;

public class GameRoom extends BaseGameRoom {

    // used for general rounds (usually phase-based turns)
    private TimedEvent roundTimer = null;

    // used for granular turn handling (usually turn-order turns)
    private TimedEvent turnTimer = null;
    private List<ServerThread> turnOrder = new ArrayList<>();
    private long currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
    private int round = 0;
    private Grid grid = new Grid(); // generated in onSessionStart()
    private Deck deck; // initialized in onSessionStart()

    public GameRoom(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClientAdded(ServerThread sp) {
        // sync GameRoom state to new client

        syncCurrentPhase(sp);
        // sync only what's necessary for the specific phase
        // if you blindly sync everything, you'll get visual artifacts/discrepancies
        syncReadyStatus(sp);
        syncAwayStatus(sp);
        if (currentPhase != Phase.READY) {
            syncTurnStatus(sp); // turn/ready use the same visual process so ensure turn status is only called
                                // outside of ready phase
            syncPlayerPoints(sp);
        }

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
        roundTimer.setTickCallback((time) -> {
            System.out.println("Round Time: " + time);
            sendCurrentTime(TimerType.ROUND, time);
        });
    }

    private void resetRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
            roundTimer = null;
            sendCurrentTime(TimerType.ROUND, -1);
        }
    }

    private void startTurnTimer() {
        turnTimer = new TimedEvent(30, () -> onTurnEnd());
        turnTimer.setTickCallback((time) -> {
            System.out.println("Turn Time: " + time);
            sendCurrentTime(TimerType.TURN, time);
        });
    }

    private void resetTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
            sendCurrentTime(TimerType.TURN, -1);
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
        round = 0;
        // keep dimensions in sync with Client's Grid
        grid.generate(5, 5, true);
        LoggerUtil.INSTANCE.info(TextFX.colorize("Grid generated: " + grid, Color.PURPLE));
        deck = new Deck(); // initialize the deck for the session
        LoggerUtil.INSTANCE.info("Deck initialized for the session");
        LoggerUtil.INSTANCE.info("onSessionStart() end");
        onRoundStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onRoundStart() {
        LoggerUtil.INSTANCE.info("onRoundStart() start");
        resetRoundTimer();
        resetTurnStatus();
        // every round, add cards to each player's hand until they have 5 cards
        turnOrder.forEach(player -> {
            while (player.getCards().size() < 5) {
                Card card = deck.drawCard();
                if (card == null) {
                    // no more cards to draw
                    break;
                }
                player.addCard(card);
                // send card to client
            }
        });
        // sync current hand to respective player
        turnOrder.forEach(player -> {
            boolean failedToSync = !player.sendCurrentHand();// doesn't take data because the hand-state is internal
            if (failedToSync) {
                removeClient(player);
            }
        });

        round++;
        // relay(null, String.format("Round %d has started", round));
        sendGameEvent(String.format("Round %d has started", round));
        // startRoundTimer(); Round timers aren't needed for turns
        // if you do decide to use it, ensure it's reasonable and based on the number of
        // players
        LoggerUtil.INSTANCE.info("onRoundStart() end");
        onTurnStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void onTurnStart() {
        LoggerUtil.INSTANCE.info("onTurnStart() start");
        resetTurnTimer();
        try {
            ServerThread currentPlayer = getNextPlayer();
            // handle away status
            if (currentPlayer.isAway()) {
                sendGameEvent(String.format("%s is currently away and is getting skipped",
                        currentPlayer.getDisplayName()));

                onTurnEnd(); // skip the turn
                return;
            }
            // relay(null, String.format("It's %s's turn", currentPlayer.getDisplayName()));
            sendGameEvent(String.format("It's %s's turn", currentPlayer.getDisplayName()));
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
            // optionally can use checkAllTookTurn();
            if (isLastPlayer()) {
                // if the current player is the last player in the turn order, end the round
                onRoundEnd();
            } else {
                onTurnStart();
            }
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
        if (round >= 10) {
            onSessionEnd();
        } else {
            onRoundStart();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onSessionEnd() {
        LoggerUtil.INSTANCE.info("onSessionEnd() start");

        // sort and print out score board
        StringBuilder scoreBoard = new StringBuilder("Score Board:\n");
        clientsInRoom.values().stream()
                .sorted((sp1, sp2) -> Integer.compare(sp2.getPoints(), sp1.getPoints()))
                .forEach(
                        sp -> scoreBoard.append(
                                String.format("<div>%s: %d points</div>", sp.getDisplayName(), sp.getPoints())));
        LoggerUtil.INSTANCE.info(scoreBoard.toString());
        sendGameEvent(scoreBoard.toString());
        // find winner with highest getPoints();
        ServerThread winner = turnOrder.stream()
                .max((sp1, sp2) -> Integer.compare(sp1.getPoints(), sp2.getPoints()))
                .orElse(null);
        if (winner != null && winner.getPoints() > 0) {
            sendGameEvent(String.format("Session ended! %s wins with %d points!",
                    winner.getDisplayName(), winner.getPoints()));
        } else {
            sendGameEvent("Session ended! No winner could be determined.");
        }
        grid.reset();
        turnOrder.clear();
        currentTurnClientId = Constants.DEFAULT_CLIENT_ID;
        // resetReadyStatus();
        // resetTurnStatus();
        /*
         * Will leverage phase change to READY to tell clients to reset all session
         * data.
         * More efficient than having a ton of different reset methods for ultimately
         * the same goal.
         * Some reset methods may still be needed, but this will cover most of the
         * session reset logic.
         */
        changePhase(Phase.READY);
        LoggerUtil.INSTANCE.info("onSessionEnd() end");
    }
    // end lifecycle methods

    // send/sync data to ServerThread(s)

    private void syncAwayStatus(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendAwayStatus(serverUser.getClientId(),
                        serverUser.isAway(), true);
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list", serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    private void sendAwayStatus(ServerThread sp) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendAwayStatus(sp.getClientId(), sp.isAway(), false);
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void syncPlayerPoints(ServerThread incomingClient) {
        clientsInRoom.values().forEach(serverUser -> {
            if (serverUser.getClientId() != incomingClient.getClientId()) {
                boolean failedToSync = !incomingClient.sendPlayerPoints(serverUser.getClientId(),
                        serverUser.getPoints());
                if (failedToSync) {
                    LoggerUtil.INSTANCE.warning(
                            String.format("Removing disconnected %s from list",
                                    serverUser.getDisplayName()));
                    disconnect(serverUser);
                }
            }
        });
    }

    private void sendPlayerPoints(ServerThread sp) {
        clientsInRoom.values().removeIf(spInRoom -> {
            boolean failedToSend = !spInRoom.sendPlayerPoints(sp.getClientId(),
                    sp.getPoints());
            if (failedToSend) {
                removeClient(spInRoom);
            }
            return failedToSend;
        });
    }

    private void sendCaughtFishUpdate(ServerThread client, int x, int y, CatchData caughtFish) {
        clientsInRoom.values().forEach(spInRoom -> {
            boolean failedToSend = !spInRoom.sendCaughtFishUpdate(client.getClientId(), x, y, caughtFish);
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

    /**
     * Sets `turnOrder` to a shuffled list of players who are ready.
     */
    private void setTurnOrder() {
        turnOrder.clear();
        turnOrder = clientsInRoom.values().stream().filter(ServerThread::isReady).collect(Collectors.toList());
        Collections.shuffle(turnOrder);
    }

    /**
     * Gets the current player based on the `currentTurnClientId`.
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
    private ServerThread getCurrentPlayer() throws MissingCurrentPlayerException, PlayerNotFoundException {
        // quick early exit
        if (currentTurnClientId == Constants.DEFAULT_CLIENT_ID) {
            throw new MissingCurrentPlayerException("Current Player not set");
        }
        return turnOrder.stream()
                .filter(sp -> sp.getClientId() == currentTurnClientId)
                .findFirst()
                // this shouldn't occur but is included as a "just in case"
                .orElseThrow(() -> new PlayerNotFoundException("Current player not found in turn order"));
    }

    /**
     * Gets the next player in the turn order.
     * If the current player is the last in the turn order, it wraps around
     * (round-robin).
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
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

    /**
     * Checks if the current player is the last player in the turn order.
     * 
     * @return
     * @throws MissingCurrentPlayerException
     * @throws PlayerNotFoundException
     */
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
            // relay(null,
            // String.format("All players have taken their turn (%d/%d) ending the round",
            // numTookTurn, numReady));
            sendGameEvent(
                    String.format("All players have taken their turn (%d/%d) ending the round", numTookTurn, numReady));
            onRoundEnd();
        }
    }

    // start check methods
    private void checkIsAway(ServerThread currentUser) throws IsAwayException {
        if (currentUser.isAway()) {
            throw new IsAwayException("You are currently away and cannot take actions");
        }
    }

    private void checkCurrentPlayer(long clientId) throws NotPlayersTurnException {
        if (currentTurnClientId != clientId) {
            throw new NotPlayersTurnException("You are not the current player");
        }
    }

    private void checkTookTurn(ServerThread currentUser) throws NotPlayersTurnException {
        if (currentUser.didTakeTurn()) {
            throw new NotPlayersTurnException("You have already taken your turn this round");
        }
    }

    private void checkCoordinateBounds(int x, int y) {
        if (grid == null || !grid.isValidCoordinate(x, y)) {
            throw new IllegalArgumentException(String.format("Coordinates (%d, %d) are out of bounds", x, y));
        }
    }
    // end check methods

    // receive data from ServerThread (GameRoom specific)
    protected void handleAwayAction(ServerThread currentUser) {
        try {
            checkPlayerInRoom(currentUser);
            // anyone can be away whenever so there are less required "checks" here
            // toggle away status
            currentUser.setAway(!currentUser.isAway());
            sendAwayStatus(currentUser);
        } catch (PlayerNotFoundException e) {
            currentUser.sendGameEvent("You must be in a GameRoom to do the away action");
            LoggerUtil.INSTANCE.severe("handleAwayAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleAwayAction exception", e);
        }
    }

    protected void handleUseCard(ServerThread currentUser, int x, int y, List<Card> cards) {
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
            checkTookTurn(currentUser);
            checkIsAway(currentUser);

            // server-side turn control (in my case, using a card isn't the end of a turn)
            // currentUser.setTookTurn(true);
            Card incoming = cards.get(0); // don't trust data from the client, just getting it for the id
            Card card = currentUser.removeCard(incoming); // check if it's in hand
            if (card == null) {
                currentUser.sendGameEvent("You don't have this card in your hand");
                return;
            }
            if (card.requiresCoordinates()) {
                checkCoordinateBounds(x, y);
                // process the card action
                switch (card.getType()) {
                    /*
                     * Unused in this section
                     * case CATCH_MULTIPLIER:
                     * break;
                     * case FISHING_ATTEMPTS:
                     * break;
                     */
                    case LONG_TERM_PROBABILITY:
                        grid.changeLongTermProbability(x, y, card.getValue());
                        sendGameEvent(TextFX.colorize(
                                String.format("Permanently change the chance of catching fish at (%d, %d) by %s",
                                        x, y, card.getValue()),
                                Color.GREEN, true));
                        break;
                    case TEMPORARY_PROBABILITY:
                        grid.changeTempProbability(x, y, card.getValue());
                        sendGameEvent(TextFX.colorize(
                                String.format("Temporarily change the chance of catching fish at (%d, %d) by %s",
                                        x, y, card.getValue()),
                                Color.GREEN, true));
                        break;
                    default:
                        LoggerUtil.INSTANCE.warning(
                                String.format("[Coords] Card type %s not implemented yet", card.getType().name()));
                        break;

                }
            } else {
                // process the card action
                switch (card.getType()) {
                    case CATCH_MULTIPLIER:
                        currentUser.adjustCatchMultiplier(card.getValue());
                        LoggerUtil.INSTANCE.info(
                                String.format("[User] %s adjusted catch multiplier by %s, result %s",
                                        currentUser.getDisplayName(),
                                        card.getValue(), currentUser.getCatchMultiplier()));
                        sendGameEvent(TextFX.colorize(
                                String.format("%s adjusted their catch multiplier by %s, result %s",
                                        currentUser.getDisplayName(), card.getValue(),
                                        currentUser.getCatchMultiplier()),
                                Color.GREEN, true));
                        break;
                    case FISHING_ATTEMPTS:
                        currentUser.adjustFishingAttempts((int) card.getValue());
                        LoggerUtil.INSTANCE.info(
                                String.format("[User] %s adjusted fishing attempts by %s, result %s",
                                        currentUser.getDisplayName(), card.getValue(),
                                        currentUser.getFishingAttempts()));
                        sendGameEvent(TextFX.colorize(
                                String.format("%s adjusted their fishing attempts by %s, result %s",
                                        currentUser.getDisplayName(), card.getValue(),
                                        currentUser.getFishingAttempts()),
                                Color.GREEN, true));
                        break;
                    /*
                     * Unused in this section
                     * case LONG_TERM_PROBABILITY:
                     * break;
                     * case TEMPORARY_PROBABILITY:
                     * break;
                     */
                    default:
                        LoggerUtil.INSTANCE.warning(
                                String.format("[User] Card type %s not implemented yet", card.getType().name()));
                        break;

                }
            }

            // sync to user
            currentUser.sendModifyHand(card, false);
            // don't end turn, using cards is an optional step

        } catch (IllegalArgumentException e) {
            currentUser.sendGameEvent("Invalid coordinates for card action");
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        } catch (NotPlayersTurnException e) {
            currentUser.sendGameEvent("It's not your turn");
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            currentUser.sendGameEvent("Spectators cannot use cards");
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendGameEvent("You must be in a GameRoom to do the card action");
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendGameEvent(
                    "You can only use cards during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        } catch (IsAwayException e) {
            currentUser.sendGameEvent("You are currently away and cannot take actions");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleUseCard exception", e);
        }
    }

    protected void handleCastAction(ServerThread currentUser, int x, int y) {
        // this action should be done last in the turn
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
            checkTookTurn(currentUser);
            checkIsAway(currentUser);
            checkCoordinateBounds(x, y);
            // server-side turn control
            currentUser.setTookTurn(true);
            // handle multiple attempts (for simplicity, attempts are used on the same cell
            // rather than letting the user pick new locations)
            int fishingAttempts = currentUser.getFishingAttempts() + 1;
            for (int i = 0; i < fishingAttempts; i++) {
                sendGameEvent(TextFX.colorize(String.format("%s is casting at (%d, %d) (attempt %d/%d)",
                        currentUser.getDisplayName(), x, y, i + 1, fishingAttempts), Color.BLUE, true));
                CatchData fishCaught = grid.tryCatchFish(x, y);
                if (fishCaught == null || fishCaught.getQuantity() <= 0) {
                    if (!grid.hasFish(x, y)) {
                        // used to mark cell as empty
                        sendCaughtFishUpdate(currentUser, x, y, new CatchData(null, 0));
                    }
                    sendGameEvent(TextFX.colorize(String.format("%s tried to catch fish at (%d, %d) but caught nothing",
                            currentUser.getDisplayName(), x, y), Color.RED, true));
                } else {
                    sendGameEvent(
                            TextFX.colorize(String.format("%s caught %s at (%d, %d)", currentUser.getDisplayName(),
                                    fishCaught, x, y), Color.GREEN, true));
                    // handle multiplier (applies to each fish attempt)
                    float mod = currentUser.getCatchMultiplier();
                    if (mod != 0) {
                        int bonus = (int) (fishCaught.getQuantity() * mod);
                        fishCaught.changeQuantity(bonus);
                    }
                    // update server state
                    currentUser.addFish(fishCaught.getFishType(), fishCaught.getQuantity());
                    // sync to clients
                    sendCaughtFishUpdate(currentUser, x, y, fishCaught);
                    sendPlayerPoints(currentUser);
                }
            }
            currentUser.resetFishingAttempts();
            currentUser.resetCatchMultiplier();

            LoggerUtil.INSTANCE.info(TextFX.colorize("Current Grid: " + grid, Color.PURPLE));
            // sync turn status (could eventually be redundant depending on project logic)
            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            // trigger end of turn
            onTurnEnd();
        } catch (IllegalArgumentException e) {
            currentUser.sendGameEvent("Invalid coordinates for fishing action");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (NotPlayersTurnException e) {
            currentUser.sendGameEvent("It's not your turn");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            currentUser.sendGameEvent("Spectators cannot fish");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendGameEvent("You must be in a GameRoom to do the fishing action");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendGameEvent(
                    "You can only fish during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (IsAwayException e) {
            currentUser.sendGameEvent("You are currently away and cannot take actions");
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleFishAction exception", e);
        }
    }

    /**
     * Handles the turn action from the client.
     * 
     * @param currentUser
     * @param exampleText (arbitrary text from the client, can be used for
     *                    additional actions or information)
     */
    @Deprecated
    protected void handleTurnAction(ServerThread currentUser, String exampleText) {
        // check if the client is in the room
        try {
            checkPlayerInRoom(currentUser);
            checkCurrentPhase(currentUser, Phase.IN_PROGRESS);
            checkCurrentPlayer(currentUser.getClientId());
            checkIsReady(currentUser);
            if (currentUser.didTakeTurn()) {
                currentUser.sendGameEvent("You have already taken your turn this round");
                return;
            }
            // example points
            /*
             * int points = new Random().nextInt(4) == 3 ? 1 : 0;
             * sendGameEvent(String.format("%s %s", currentUser.getDisplayName(),
             * points > 0 ? "gained a point" : "didn't gain a point"));
             * if (points > 0) {
             * currentUser.changePoints(points);
             * sendPlayerPoints(currentUser);
             * }
             */
            currentUser.setTookTurn(true);
            // TODO handle example text possibly or other turn related intention from client

            sendTurnStatus(currentUser, currentUser.didTakeTurn());
            // finished processing the turn
            onTurnEnd();
        } catch (NotPlayersTurnException e) {
            currentUser.sendGameEvent("It's not your turn");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (NotReadyException e) {
            // The check method already informs the currentUser
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PlayerNotFoundException e) {
            currentUser.sendGameEvent("You must be in a GameRoom to do the ready check");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (PhaseMismatchException e) {
            currentUser.sendGameEvent(
                    "You can only take a turn during the IN_PROGRESS phase");
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        } catch (Exception e) {
            LoggerUtil.INSTANCE.severe("handleTurnAction exception", e);
        }
    }

    // end receive data from ServerThread (GameRoom specific)
}