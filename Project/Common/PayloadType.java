package Project.Common;

public enum PayloadType {
       CLIENT_CONNECT, // client requesting to connect to server (passing of initialization data
                       // [name])
       CLIENT_ID, // server sending client id
       SYNC_CLIENT, // silent syncing of clients in room
       DISCONNECT, // distinct disconnect action
       ROOM_CREATE,
       ROOM_JOIN,
       ROOM_LEAVE,
       REVERSE,
       MESSAGE, // sender and message,
       ROOM_LIST, // list of rooms
       READY, // client to trigger themselves as ready, server to sync the related status of a
              // particular client
       SYNC_READY, // quiet version of READY, used to sync existing ready status of clients in a
                   // GameRoom
       RESET_READY, // trigger to tell the client to reset their whole local list's ready status
                    // (saves network requests)
       PHASE, // syncs current phase of session (used as a switch to only allow certain logic
              // to execute)
       TURN, // example of taking a turn and syncing a turn action
       SYNC_TURN, // quiet version of TURN, used to sync existing turn status of clients in a
                  // GameRoom
       RESET_TURN, // trigger to tell client to reset their local list turn status
}
