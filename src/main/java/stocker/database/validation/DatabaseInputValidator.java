package stocker.database.validation;

import stocker.model.Candlestick;
import java.util.List;

/**
 * Validator class for database input used by {@link stocker.database.CandlestickDao}.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class DatabaseInputValidator {

    /**
     * Validates stock symbol input.
     *
     * @param symbol the stock symbol to validate
     * @throws IllegalArgumentException if the symbol is null or empty
     */
    public void validateSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Stock symbol cannot be null");
        }
        if (symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Stock symbol cannot be empty");
        }
    }

    /**
     * Validates a list of candlesticks.
     *
     * @param candlesticks the list of candlesticks to validate
     * @throws IllegalArgumentException if the candlesticks list is null or empty
     */
    public void validateCandlesticksList(List<Candlestick> candlesticks) {
        if (candlesticks == null) {
            throw new IllegalArgumentException("Candlesticks list cannot be null");
        }
        if (candlesticks.isEmpty()) {
            throw new IllegalArgumentException("Candlesticks list cannot be empty");
        }
    }

    /**
     * Validates an individual candlestick for required data integrity.
     * Validation includes:
     * - Timestamp must be positive
     * - Open, close, high, and low prices must be non-negative
     * - Volume must be non-negative
     * - High price must be greater than or equal to low price
     *
     * @param candlestick the candlestick to validate
     * @throws IllegalArgumentException if the candlestick is null or has invalid data
     */
    public void validateCandlestick(Candlestick candlestick) {
        if (candlestick == null) {
            throw new IllegalArgumentException("Candlestick cannot be null");
        }

        // Validate timestamp
        if (candlestick.getTimestamp() <= 0) {
            throw new IllegalArgumentException("Candlestick timestamp must be positive");
        }

        // Validate prices are not negative
        if (candlestick.getOpen() < 0) {
            throw new IllegalArgumentException("Candlestick open price cannot be negative");
        }
        if (candlestick.getClose() < 0) {
            throw new IllegalArgumentException("Candlestick close price cannot be negative");
        }
        if (candlestick.getHigh() < 0) {
            throw new IllegalArgumentException("Candlestick high price cannot be negative");
        }
        if (candlestick.getLow() < 0) {
            throw new IllegalArgumentException("Candlestick low price cannot be negative");
        }

        // Validate volume is not negative
        if (candlestick.getVolume() < 0) {
            throw new IllegalArgumentException("Candlestick volume cannot be negative");
        }

        // Validate price relationships (high >= low, etc.)
        if (candlestick.getHigh() < candlestick.getLow()) {
            throw new IllegalArgumentException("Candlestick high price cannot be less than low price");
        }
        if (candlestick.getOpen() > candlestick.getHigh() || candlestick.getOpen() < candlestick.getLow()) {
            throw new IllegalArgumentException("Candlestick open price must be between high and low prices");
        }
        if (candlestick.getClose() > candlestick.getHigh() || candlestick.getClose() < candlestick.getLow()) {
            throw new IllegalArgumentException("Candlestick close price must be between high and low prices");
        }
    }

    /**
     * Validates all candlesticks in a list.
     *
     * @param candlesticks the list of candlesticks to validate
     * @throws IllegalArgumentException if any candlestick is invalid
     */
    public void validateAllCandlesticks(List<Candlestick> candlesticks) {
        validateCandlesticksList(candlesticks); // First validate the list itself

        for (int i = 0; i < candlesticks.size(); i++) {
            try {
                validateCandlestick(candlesticks.get(i));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid candlestick at index " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Validates database connection string format.
     * This is a basic format check, not a connection test.
     *
     * @param dbUrl the database URL to validate
     * @throws IllegalArgumentException if the URL format is invalid
     */
    public void validateDatabaseUrl(String dbUrl) {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Database URL cannot be null or empty");
        }

        // Basic validation
        String url = dbUrl.trim().toLowerCase();
        if (!url.startsWith("jdbc:")) {
            throw new IllegalArgumentException("Database URL must start with 'jdbc:'");
        }
    }

    /**
     * Validates database username.
     *
     * @param username the database username to validate
     * @throws IllegalArgumentException if the username is null or empty
     */
    public void validateDatabaseUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Database username cannot be null or empty");
        }
    }

    /**
     * Validates that a database password is not null.
     * Empty passwords are allowed.
     *
     * @param password the database password to validate
     * @throws IllegalArgumentException if the password is null
     */
    public void validateDatabasePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Database password cannot be null (empty is allowed)");
        }
    }
}