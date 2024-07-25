package stocker.support;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * This class is used to check whether the Swedish stock market is closed (outside weekends).
 * <p>
 * The class uses {@link MarketCalendarFetcher} to get a {@link java.util.List<LocalDate>} of dates when the market is closed.
 * </p>
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class MarketState {
    private static final List<LocalDate> dates = MarketCalendarFetcher.fetchMarketCalendar();

    /**
     * Checks if the stock market is open on the given date.
     *
     * @param date the date to check
     * @return {@code true} if the market is open on the given date, {@code false} otherwise
     */
    public static boolean isOpen(final LocalDate date) {
        return !dates.contains(date) && !isWeekend(date);
    }

    /**
     * Checks if the stock market is closed on the given date.
     *
     * @param date the date to check
     * @return {@code true} if the market is closed on the given date, {@code false} otherwise
     */
    public static boolean isClosed(final LocalDate date) {
        return dates.contains(date) || isWeekend(date);
    }

    /**
     * Checks if the given date falls on a weekend.
     *
     * @param date the date to check
     * @return {@code true} if the given date is a Saturday or Sunday, {@code false} otherwise
     */
    private static boolean isWeekend(final LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
