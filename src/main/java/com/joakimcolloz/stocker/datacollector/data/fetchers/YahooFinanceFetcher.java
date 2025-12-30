package com.joakimcolloz.stocker.datacollector.data.fetchers;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Yahoo Finance data fetcher that retrieves stock data from the Yahoo Finance API.
 */
public class YahooFinanceFetcher extends BaseDataFetcher {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties() // Check system env as fallback
            .load();

    private static final String YAHOO_API_KEY_HEADER = "X-RapidAPI-Key";
    private static final String YAHOO_API_HOST_HEADER = "X-RapidAPI-Host";
    private static final String YAHOO_API_HOST = "stock-data-yahoo-finance-alternative.p.rapidapi.com";
    private static final String YAHOO_API_URL = "https://stock-data-yahoo-finance-alternative.p.rapidapi.com" +
            "/v8/finance/chart/";

    protected String buildApiUrl(String stockName, String range, String interval) {
        return YAHOO_API_URL + stockName + "?range=" + range + "&interval=" + interval;
    }

    public YahooFinanceFetcher() {
        super(
            YAHOO_API_KEY_HEADER,
            YAHOO_API_HOST_HEADER,
            dotenv.get("RAPID_API_KEY"),
            YAHOO_API_HOST,
            YAHOO_API_URL
        );
    }
}




