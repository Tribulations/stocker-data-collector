package stocker.support;

/**
 * CLass containing methods used for debugging etc.
 */
public final class Utils {


    // todo this method can add performance issues etc. only use for debugging
    public static String getMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 3) {
            return stackTrace[2].getMethodName();
        } else {
            return "Unknown";
        }
    }
}
