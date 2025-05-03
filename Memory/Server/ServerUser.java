package Memory.Server;

import Memory.Common.Phase;
import Memory.Common.User;

public class ServerUser extends User {
    private ServerThread client;

    public ServerUser(ServerThread client) {
        this.client = client;
        setClientId(client.getClientId());
        setClientName(client.getClientName());
    }

    /**
     * Used only for passing the ServerThread to the base class of Room.
     * Favor creating wrapper methods instead of interacting with this directly.
     * 
     * @return ServerThread reference
     */
    public ServerThread getServerThread() {
        return client;
    }

    // add any wrapper methods to call on the ServerThread
    // don't used the exposed full ServerThread object
    public boolean sendTurnStatus(long clientId, boolean didTakeTurn) {
        return sendTurnStatus(clientId, didTakeTurn, false);
    }

    public boolean sendTurnStatus(long clientId, boolean didTakeTurn, boolean quiet) {
        return client.sendTurnStatus(clientId, didTakeTurn, quiet);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady, boolean quiet) {
        return client.sendReadyStatus(clientId, isReady, quiet);
    }

    public boolean sendReadyStatus(long clientId, boolean isReady) {
        return client.sendReadyStatus(clientId, isReady);
    }

    public boolean sendResetReady() {
        return client.sendResetReady();
    }

    public boolean sendCurrentPhase(Phase phase) {
        return client.sendCurrentPhase(phase);
    }
}
