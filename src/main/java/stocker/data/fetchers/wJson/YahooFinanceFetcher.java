package stocker.data.fetchers.wJson;

/**
 *
 */
public class YahooFinanceFetcher extends BaseDataFetcher {
    public static final YahooFinanceFetcher INSTANCE = new YahooFinanceFetcher();

    private YahooFinanceFetcher()  {// TODO add these values as constants
        super("X-RapidAPI-Key", "X-RapidAPI-Host",
                "3a491bbe0amshadc3d607a3d7e2dp1c9ee5jsnb82d483841a2",
                "stock-data-yahoo-finance-alternative.p.rapidapi.com",
                "https://stock-data-yahoo-finance-alternative.p.rapidapi.com/v8/finance/chart/");
    }
}




