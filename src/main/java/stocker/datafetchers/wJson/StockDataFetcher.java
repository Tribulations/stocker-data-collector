package stocker.datafetchers.wJson;

import stocker.support.StockAppLogger;
import stocker.support.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Class used to fetch json stock data from the Yahoo financial data API.
 * TODO add api host stuff to constants class. make this class a bass class and derive concrete data fetches for different apps e.g. YahooFinance/TwelveDataFetcher. these classes can share the fetch() because they all can use the same HttpRequest client etc.
 *
 */
public final class StockDataFetcher {
    private final String API_KEY_HEADER = "X-RapidAPI-Key";
    private final String API_HOST_HEADER = "X-RapidAPI-Host";
    private final String API_KEY = "3a491bbe0amshadc3d607a3d7e2dp1c9ee5jsnb82d483841a2"; // This is not secure? TODO
    private final String API_HOST = "stock-data-yahoo-finance-alternative.p.rapidapi.com";
    public static final StockDataFetcher INSTANCE = new StockDataFetcher();

    private String jsonResponseString; // Stores the previous fetched stock data

    /**
     * Default private construction TODO
     */
    private StockDataFetcher()  {
    }

    /**
     * Fetches stock data and returns the response as a string in json format, as well as
     * assigning this String value to the class member field {@link #jsonResponseString}.
     * @return the json string of stock data
     */
    public String fetchStockData(final String stockName, final String range, final String interval) {
        String API_URL = "https://stock-data-yahoo-finance-alternative.p.rapidapi.com/v8/finance/chart/"
                + stockName + "?range=" + range + "&interval=" + interval;

        StockAppLogger.INSTANCE.logInfo(
                String.format("Fetched data: %s, range: %s, interval: %s - %s::%s", stockName, range,
                        interval, getClass().getCanonicalName(), Utils.getMethodName()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header(API_KEY_HEADER, API_KEY)
                .header(API_HOST_HEADER, API_HOST)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(
                    request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.jsonResponseString = response.body(); // save response as member
        StockAppLogger.INSTANCE.logInfo(jsonResponseString);

        return response.body();
    }

    /**
     *
     * @return the previously fetched stock data
     */
    public String getJsonResponseString() {
        return jsonResponseString;
    }
}



