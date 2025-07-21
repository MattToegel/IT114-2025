package Project.Client.Interfaces;

/**
 * Interface for handling client connection events.
 */
public interface IConnectionEvents extends IClientEvents {
    // no need for onClientConnect() as IRoomEvents handles it as joining

    /**
     * Triggered when a client disconnects.
     *
     * @param id         The client ID.
     * @param clientName The client name.
     */
    void onClientDisconnect(long id);

    /**
     * Received the server-given ID for our client reference.
     *
     * @param id The client ID.
     */
    void onReceiveClientId(long id);
}