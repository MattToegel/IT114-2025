package NDFF.Exceptions;

public class NotPlayersTurnException extends CustomIT114Exception {
    public NotPlayersTurnException(String message) {
        super(message);
    }

    public NotPlayersTurnException(String message, Throwable cause) {
        super(message, cause);
    }
}