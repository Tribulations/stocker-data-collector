package stocker.support;

import java.io.IOException;
import java.util.logging.*;

/**
 *  singleton logger eager init.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public final class StockAppLogger {
    // eager initialization of the class's single instance
    public static final StockAppLogger INSTANCE = new StockAppLogger();
    private final Logger debugLogger;
    private final Logger infoLogger;



    /**
     * Private construction to only allow a single instance of the class.
     */
    private StockAppLogger() {
        this.debugLogger = Logger.getLogger(this.getClass().getCanonicalName() + ".debug");
        this.debugLogger.setLevel(Level.ALL);

        this.infoLogger = Logger.getLogger(this.getClass().getCanonicalName() + ".info");

        // init formatters
       final Formatter infoFormatter = new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                return logRecord.getLevel() + " " + new java.util.Date() + " : "
                        + " - " + logRecord.getMessage() + "\n";
            }
        };
        final Formatter debugFormatter = new Formatter() {
            @Override
            public String format(LogRecord logRecord) {
                return String.format("%s%n", logRecord.getMessage());
            }
        };

        try {
            // custom formatting of handlers
            this.debugLogger.setUseParentHandlers(false);
            this.infoLogger.setUseParentHandlers(false);

            // custom console handler
            ConsoleHandler consoleDebugHandler = new ConsoleHandler();

            FileHandler fileInfoHandler = new FileHandler(
                    "src/main/resources/info%g.log", 50000, 5, true);

            // Set custom formatters
            consoleDebugHandler.setFormatter(debugFormatter);
            fileInfoHandler.setFormatter(infoFormatter);

            this.debugLogger.addHandler(consoleDebugHandler);
            this.infoLogger.addHandler(fileInfoHandler);

            consoleDebugHandler.setLevel(Level.ALL);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called internally from the different logging methods to perform logging all the message provided as argument.
     * @param logMessage the message to log
     */
    public void logInfo(final String logMessage) {
        infoLogger.info(logMessage);
    }

    public void logDebug(final String logMessage) {
//        logger.finest(logMessage);
        debugLogger.finest(logMessage);

    }

    public void closeHandlers() {
        for (Handler h : debugLogger.getHandlers()) {
            h.close();
        }

        for (Handler h : infoLogger.getHandlers()) {
            h.close();
        }
    }

//    public Logger getDebugLogger() {
//        return debugLogger;
//    }
//
//    public Logger getInfoLogger() {
//        return infoLogger;
//    }

    public void turnOffDebugLogging() {
        debugLogger.setLevel(Level.OFF);
    }

    public void turnOffInfoLogging() {
        infoLogger.setLevel(Level.OFF);
    }

    public void turnOnDebugLogging() {
        debugLogger.setLevel(Level.ALL);
    }

    public void turnOnInfoLogging() {
        infoLogger.setLevel(Level.INFO);
    }
}

