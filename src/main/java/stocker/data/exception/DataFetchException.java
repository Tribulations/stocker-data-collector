package stocker.data.exception;

/**
 * Exception thrown when data fetching operations fail.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class DataFetchException extends Exception {

    public DataFetchException(String message) {
        super(message);
    }

    public DataFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
