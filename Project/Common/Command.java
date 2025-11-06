package Project.Common;

import java.util.HashMap;

public enum Command {
    QUIT("quit"),
    DISCONNECT("disconnect"),
    LOGOUT("logout"),
    LOGOFF("logoff"),
    REVERSE("reverse"),
    CREATE_ROOM("createroom"),
    LEAVE_ROOM("leaveroom"),
    JOIN_ROOM("joinroom"),
    NAME("name"),
    LIST_USERS("users"),
    LIST_ROOMS("listrooms"),
    READY("ready"),;

    private static final HashMap<String, Command> BY_COMMAND = new HashMap<>();
    static {
        for (Command e : values()) {
            BY_COMMAND.put(e.command, e);
        }
    }
    public final String command;

    private Command(String command) {
        this.command = command;
    }

    public static Command stringToCommand(String command) {
        return BY_COMMAND.get(command);
    }
}
