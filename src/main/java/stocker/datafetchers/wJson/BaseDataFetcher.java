package stocker.datafetchers.wJson;


import stocker.support.StockAppLogger;
import stocker.support.Utils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Base data fetcher used as template for other concrete fetchers.
 */
public abstract class BaseDataFetcher {
    private final String API_KEY_HEADER;
    private final String API_HOST_HEADER;
    private final String API_KEY;
    private final String API_HOST;
    private final String API_URL;
    private String jsonResponseString; // Stores the previous fetched stock data

    protected BaseDataFetcher(final String API_KEY_HEADER, final String API_HOST_HEADER, final String API_KEY, final String API_HOST, final String API_URL) {
        this.API_KEY_HEADER = API_KEY_HEADER;
        this.API_HOST_HEADER = API_HOST_HEADER;
        this.API_KEY = API_KEY;
        this.API_HOST = API_HOST;
        this.API_URL = API_URL;
    }

    /**
     * Fetches stock data and returns the response as a string in json format, as well as
     * assigning this String value to the class member field {@link #jsonResponseString}.
     * @return the json string of stock data
     */
    public String fetchData(final String stockName, final String range, final String interval) {
        String fetchUrl = API_URL + stockName + "?range=" + range + "&interval=" + interval; // todo change fetchUrl to apiUrl?

        StockAppLogger.INSTANCE.logInfo(
                String.format("Fetched data: %s, range: %s, interval: %s - %s::%s", stockName, range,
                        interval, getClass().getCanonicalName(), Utils.getMethodName()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fetchUrl))
                .header(API_KEY_HEADER, API_KEY)
                .header(API_HOST_HEADER, API_HOST)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(
                    request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e); // todo catch this here and handle the exception? if we get an exception it is because we couldn't connect ovet http? so hanxle this exception jere!!
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




