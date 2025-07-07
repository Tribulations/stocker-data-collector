package stocker.data.fetchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Base data fetcher used as template for other concrete fetchers.
 */
public abstract class BaseDataFetcher { // TODO rename package wJson to json
    private static final Logger logger = LoggerFactory.getLogger(BaseDataFetcher.class);
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
        String apiUrl = API_URL + stockName + "?range=" + range + "&interval=" + interval;

        logger.info("Fetching data for stock: {}, range: {}, interval: {}", stockName, range, interval);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header(API_KEY_HEADER, API_KEY)
                .header(API_HOST_HEADER, API_HOST)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en-US,en;q=0.9")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(
                    request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            logger.error("Couldn't get an http response: Check the connection to the API. {}", e.getMessage(), e);
        }
        if (response != null) {
            this.jsonResponseString = response.body(); // save response as member
            logger.info(jsonResponseString);
            logger.debug(jsonResponseString);
        } else {
            logger.error("HTTP request failed: Response was null");
            logger.debug("HTTP request failed: Response was null");
        }

        return response != null ? response.body() : null;
    }

    /**
     * Accessor returning the previously fetched json as a string.
     * @return the previously fetched stock data
     */
    public String getJsonResponseString() {
        return jsonResponseString;
    }
}
