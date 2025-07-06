package stocker.data.fetchers;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Yahoo Finance data fetcher that retrieves stock data from the Yahoo Finance API.
 */
public class YahooFinanceFetcher extends BaseDataFetcher {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    public static final YahooFinanceFetcher INSTANCE = new YahooFinanceFetcher();

    private YahooFinanceFetcher() {
        super(
            dotenv.get("YAHOO_API_KEY_HEADER"),
            dotenv.get("YAHOO_API_HOST_HEADER"),
            dotenv.get("YAHOO_API_KEY"),
            dotenv.get("YAHOO_API_HOST"),
            dotenv.get("YAHOO_API_URL")
        );
    }
}




