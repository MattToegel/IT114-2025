package Project.Exceptions;

public class NotReadyException extends CustomIT114Exception {
    public NotReadyException(String message) {
        super(message);
    }

    public NotReadyException(String message, Throwable cause) {
        super(message, cause);
    }
}
