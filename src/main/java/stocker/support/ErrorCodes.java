package stocker.support;

/**
 * Class storing error code messages.
 */
public final class ErrorCodes {

    private ErrorCodes() {
        throw new IllegalStateException("Utility class");
    }

    /** NETWORK ERRORS */
    public static final String ERROR_4000 = "Couldn't get an http response: Check the connection to the API\n";
    public static final String ERROR_4001 = "Response was null";
}
