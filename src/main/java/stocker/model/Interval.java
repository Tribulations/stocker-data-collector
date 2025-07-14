package stocker.model;

/**
 * Enum representing different valid intervals.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public enum Interval {
    ONE_MINUTE,
    FIVE_MINUTES,
    FIFTEEN_MINUTES,
    ONE_HOUR,
    ONE_WEEK,
    ONE_DAY,
    FIVE_DAYS,
    ONE_MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    ONE_YEAR,
    TWO_YEAR,
    FIVE_YEARS,
    TEN_YEARS,
    YTD,
    MAX;

    @Override
    public String toString() {
        return switch (this) {
            case ONE_MINUTE -> "1m";
            case FIVE_MINUTES -> "5m";
            case FIFTEEN_MINUTES -> "15m";
            case ONE_HOUR -> "1h";
            case ONE_DAY -> "1d";
            case FIVE_DAYS -> "5d";
            case ONE_WEEK -> "1wk";
            case ONE_MONTH -> "1mo";
            case THREE_MONTHS -> "3mo";
            case SIX_MONTHS -> "6mo";
            case ONE_YEAR -> "1y";
            case TWO_YEAR -> "2y";
            case FIVE_YEARS -> "5y";
            case TEN_YEARS -> "10y";
            case YTD -> "ytd";
            case MAX -> "max";
        };
    }
}
