package Project.Exceptions;

public class PlayerNotFoundException extends CustomIT114Exception {
    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
