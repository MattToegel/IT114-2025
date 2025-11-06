package Project.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Project.Common.Payload;
import Project.Common.User;

/**
 * Base class the handles the underlying connection between Client and
 * Server-side
 */
public abstract class BaseServerThread extends Thread {

    protected boolean isRunning = false; // control variable to stop this thread
    protected ObjectOutputStream out; // exposed here for send()
    protected Socket client; // communication directly to "my" client
    protected User user = new User();
    protected Room currentRoom;

    /**
     * Returns the current Room associated with this ServerThread
     * 
     * @return
     */
    protected Room getCurrentRoom() {
        return this.currentRoom;
    }

    /**
     * Allows the setting of a non-null Room reference to this ServerThread
     * 
     * @param room
     */
    protected void setCurrentRoom(Room room) {
        if (room == null) {
            throw new NullPointerException("Room argument can't be null");
        }
        if (room == currentRoom) {
            System.out.println(
                    String.format("ServerThread set to the same room [%s], was this intentional?", room.getName()));
        }
        currentRoom = room;
    }

    /**
     * Returns the status of this ServerThread
     * 
     * @return
     */
    public boolean isRunning() {
        return isRunning;
    }

    public void setClientId(long clientId) {
        this.user.setClientId(clientId);
    }

    public long getClientId() {
        // Note: We return clientId instead of threadId as we'll change this identifier
        // in the future
        return this.user.getClientId();
    }

    /**
     * Sets the client name and triggers onInitialized()
     * 
     * @param clientName
     */
    protected void setClientName(String clientName) {
        this.user.setClientName(clientName);
        onInitialized();
    }

    public String getClientName() {
        return this.user.getClientName();
    }

    public String getDisplayName() {
        return this.user.getDisplayName();
    }

    /**
     * A wrapper method so we don't need to keep typing out the long/complex sysout
     * line inside
     * 
     * @param message
     */
    protected abstract void info(String message);

    /**
     * Triggered when object is fully initialized
     */
    protected abstract void onInitialized();

    /**
     * Receives a Payload and passes data to proper handler
     * 
     * @param payload
     */
    protected abstract void processPayload(Payload payload);

    /**
     * Sends the payload over the socket
     * 
     * @param payload
     * @return true if no errors were encountered
     */
    protected boolean sendToClient(Payload payload) {
        if (!isRunning) {
            return true;
        }
        try {
            info("Sending to client: " + payload);
            out.writeObject(payload);
            out.flush();
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        }
    }

    /**
     * Terminates the server-side of the connection
     */
    protected void disconnect() {
        if (!isRunning) {
            // prevent multiple triggers if this gets called consecutively
            return;
        }
        info("Thread being disconnected by server");
        isRunning = false;
        this.interrupt(); // breaks out of blocking read in the run() method
        cleanup(); // good practice to ensure data is written out immediately
    }

    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    if (getClientName() == null || getClientName().isBlank()) {
                        info("Client name not received. Disconnecting");
                        disconnect();
                    }
                }
            }, 3000);
            Payload fromClient;
            /**
             * isRunning is a flag to let us manage the loop exit condition
             * fromClient (in.readObject()) is a blocking method that waits until data is
             * received
             * - null would likely mean a disconnect so we use a "set and check" logic to
             * alternatively exit the loop
             */
            while (isRunning) {
                try {
                    fromClient = (Payload) in.readObject(); // blocking method
                    if (fromClient != null) {
                        info("Received from my client: " + fromClient);
                        processPayload(fromClient);
                    } else {
                        throw new IOException("Connection interrupted"); // Specific exception for a clean break
                    }
                } catch (ClassCastException | ClassNotFoundException cce) {
                    System.err.println("Error reading object as specified type: " + cce.getMessage());
                    cce.printStackTrace();
                } catch (IOException e) {
                    if (Thread.currentThread().isInterrupted()) {
                        info("Thread interrupted during read (likely from the disconnect() method)");
                        break;
                    }
                    info("IO exception while reading from client");
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    info("Unexpected exception while reading from client");
                    e.printStackTrace();
                    break;
                }
            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            info("General Exception");
            e.printStackTrace();
            info("My Client disconnected");
        } finally {
            if (currentRoom != null) {
                currentRoom.handleDisconnect(this);
            }
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    /**
     * Cleanup method to close the connection and reset the user object
     */
    protected void cleanup() {
        info("ServerThread cleanup() start");
        try {
            // close server-side end of connection
            currentRoom = null;
            out.close();
            client.close();
            user.reset();
            info("Closed Server-side Socket");
        } catch (IOException e) {
            info("Client already closed");
        }

        info("ServerThread cleanup() end");
    }
}