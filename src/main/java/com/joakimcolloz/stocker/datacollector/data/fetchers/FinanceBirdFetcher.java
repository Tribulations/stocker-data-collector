package com.joakimcolloz.stocker.datacollector.data.fetchers;

import io.github.cdimascio.dotenv.Dotenv;

// TODO: add java doc mentioning link to price plan: https://rapidapi.com/shareefbassam3/api/financebird/pricing
public class FinanceBirdFetcher extends BaseDataFetcher {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties() // Check system env as fallback
            .load();

    private static final String API_KEY_HEADER = "x-rapidapi-key";
    private static final String API_HOST_HEADER = "x-rapid-api-host";
    private static final String API_HOST = "financebird.p.rapidapi.com";
    private static final String API_URL = "https://financebird.p.rapidapi.com/quote/";
    private static final String ENDPOINT = "/history";

    @Override
    protected String buildApiUrl(String stockName, String range, String interval) {
        return API_URL + stockName + ENDPOINT + "?range=" + range + "&interval=" + interval;
    }

    public FinanceBirdFetcher() {
        super(
                API_KEY_HEADER,
                API_HOST_HEADER,
                dotenv.get("RAPID_API_KEY"),
                API_HOST,
                API_URL
        );
    }
}
