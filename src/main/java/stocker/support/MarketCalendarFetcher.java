package stocker.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to fetch data about stock market days when closed.
 * Uses Jsoup to fetch and parse HTML.
 * Source: <a href="https://www.nasdaqomxnordic.com/tradinghours/?languageId=3">NasdaqOMX calendar</a>
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class MarketCalendarFetcher {
    public MarketCalendarFetcher() {
    }

    /**
     * Fetches HTML for stock market closed dates from nasdaqomxnordic.
     * @return a list of LocalDate objects representing the closed days
     */
    public static List<LocalDate> fetchMarketCalendar() {
        List<LocalDate> localDates = new ArrayList<>();
        final String url = "https://www.nasdaqomxnordic.com/tradinghours/?languageId=3";

        try {
            Document doc = Jsoup.connect(url).get();
            final String selector = "#article-64552 > div > table > tbody > tr:nth-child(3) "
                    + "> td:nth-child(2) > p:nth-child(1)";

            Elements elements = doc.select(selector);
            if (!elements.isEmpty()) {
                // Sanitize and parse the dates
                String[] dates = sanitize(elements.get(0).text()).split(" ");
                localDates = parseDates(dates);
            }
        } catch (IOException e) {
            System.out.println(
                    "MarketCalendarFetcher::fetchMarketCalendar() threw IOException when trying to fetch URL: "
                            + url);
        }

        return localDates;
    }

    /**
     * Sanitizes the input string by removing potentially harmful content.
     * This method performs basic sanitization to remove HTML tags and special characters
     * that could be exploited in Cross-Site Scripting (XSS) attacks or other security vulnerabilities.
     * The method uses regular expressions to:
     * 1. Remove any HTML tags by replacing them with an empty string.
     * 2. Remove any characters that are not alphanumeric, spaces, commas, or periods.
     *
     * Note: This is a basic sanitization method and may not cover all cases. For more comprehensive
     * sanitization, consider using libraries specifically designed for security.
     *
     * @param input the raw input string to be sanitized
     * @return a sanitized version of the input string, with HTML tags and special characters removed
     */
    private static String sanitize(String input) {
        // Basic sanitization: remove any script tags or special characters
        return input.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9., ]", "");
    }

    /**
     * Parses dates into {@link LocalDate} objects.
     * @param dates the sanitized date strings
     * @return a {@link List} of {@link LocalDate} objects
     */
    private static List<LocalDate> parseDates(String[] dates) {
        List<LocalDate> localDates = new ArrayList<>();

        // Extract dates. The month is in the first index and the day of month in the second
        for (int i = 1; i + 1 < dates.length; i = i + 2) {
            String month = dates[i];
            String dayOfMonth = dates[i + 1].replaceAll(",", "");

            int currentYear = LocalDate.now().getYear();
            final LocalDate date = LocalDate.of(currentYear, getMonthAsInt(month), Integer.parseInt(dayOfMonth));
            localDates.add(date);
        }

        return localDates;
    }

    /**
     * Converts month abbreviations to their corresponding integer values.
     * @param monthStr the month abbreviation
     * @return the integer value of the month
     */
    private static int getMonthAsInt(final String monthStr) {
        int month = 0;
        switch (monthStr) {
            case "Jan" -> month = 1;
            case "Feb" -> month = 2;
            case "Mar" -> month = 3;
            case "Apr" -> month = 4;
            case "May" -> month = 5;
            case "Jun" -> month = 6;
            case "Jul" -> month = 7;
            case "Aug" -> month = 8;
            case "Sep" -> month = 9;
            case "Oct" -> month = 10;
            case "Nov" -> month = 11;
            case "Dec" -> month = 12;
        }

        return month;
    }
}
