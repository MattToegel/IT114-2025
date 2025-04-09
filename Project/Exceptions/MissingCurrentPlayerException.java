package Project.Exceptions;

public class MissingCurrentPlayerException extends CustomIT114Exception {
    public MissingCurrentPlayerException(String message) {
        super(message);
    }

    public MissingCurrentPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

}