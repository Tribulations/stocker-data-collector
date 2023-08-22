//package stocker.datafetchers.wJson;
//
//import stocker.support.StockAppLogger;
//import stocker.support.Utils;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
///**
// * Class used to fetch json stock data from the TWELVE data API. It is a good API though h the free version
// * gives no access to Swedish stock data.
// * <a href="https://rapidapi.com/twelvedata/api/twelve-data1/">Twelve DATA API</a>
// *
// */
//public final class TwelveDataFetcher {
//    private final String API_KEY_HEADER = "X-RapidAPI-Key";
//    private final String API_HOST_HEADER = "X-RapidAPI-Host";
//    private final String API_KEY = "3a491bbe0amshadc3d607a3d7e2dp1c9ee5jsnb82d483841a2"; // This is not secure?
//    private final String API_HOST = "twelve-data1.p.rapidapi.com";
//    public static final TwelveDataFetcher INSTANCE = new TwelveDataFetcher();
//
//    private String jsonResponseString; // Stores the previous fetched stock data
//
//    /**
//     * Default private construction
//     */
//    private TwelveDataFetcher()  {
//    }
//
//    /**
//     * Fetches stock data and returns the response as a string in json format, as well as
//     * assigning this String value to the class member field {@link #jsonResponseString}.
//     * @return the json string of stock data
//     */
//    public String fetchStockData(final String stockName, final String interval, final int outputSize) {
//        String API_URL = "https://twelve-data1.p.rapidapi.com/time_series?symbol="
//        + stockName +  "&interval=" + interval + "ay&outputsize=" + outputSize + "&format=json";
//
//        StockAppLogger.INSTANCE.logInfo(
//                String.format("Fetched data: %s, interval: %s, output size: %s - %s::%s", stockName, interval, outputSize,
//                        getClass().getCanonicalName(), Utils.getMethodName()));
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(API_URL))
//                .header(API_KEY_HEADER, API_KEY)
//                .header(API_HOST_HEADER, API_HOST)
//                .method("GET", HttpRequest.BodyPublishers.noBody())
//                .build();
//
//
//        HttpResponse<String> response = null;
//        try {
//            response = HttpClient.newHttpClient().send(
//                    request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        this.jsonResponseString = response.body(); // save response as member
//        StockAppLogger.INSTANCE.logInfo(jsonResponseString);
//
//        return response.body();
//    }
//
//    /**
//     *
//     * @return the previously fetched stock data
//     */
//    public String getJsonResponseString() {
//        return jsonResponseString;
//    }
//
//    public static void main(String... args) {
//        String response = TwelveDataFetcher.INSTANCE.fetchStockData("AMZN", "1d", 10);
//        System.out.println(response);
//    }
//}
//
//
//
