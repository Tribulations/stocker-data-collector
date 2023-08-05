package stocker;
// TODO add package.info files to all packages
import stocker.database.CandlestickDao;
import stocker.datafetchers.wJson.BaseParser;
import stocker.datafetchers.wJson.JsonConstants;
import stocker.datafetchers.wJson.YahooFinanceFetcher;
import stocker.stock.Candlestick;
import stocker.stock.Stock;
import stocker.datafetchers.wScrape.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static stocker.datafetchers.wJson.JsonConstants.*;

public class Main {
    public static void main(String[] args) throws Exception {
        testNewParser();
    }

    private static void testGetSingleCandleStick() throws Exception {
        Stock aak = new Stock("AAK.ST", ONE_DAY, ONE_DAY);
        aak.getTradingPeriod().getCandlesticks().forEach(candlestick -> {
            System.out.println(candlestick.getTimestamp());
            System.out.println(candlestick);
        });
    }

    public static void testNewParser() {
        final String jsonString = YahooFinanceFetcher.INSTANCE.fetchData("BOL.ST", ONE_MONTH, ONE_DAY);
        BaseParser baseParser = new BaseParser();
        baseParser.parse(jsonString);
    }

    private static void testStockDataFetcher() throws Exception {
        Stock boliden = new Stock("BOL.ST", JsonConstants.ONE_MONTH, JsonConstants.ONE_HOUR);
        boliden.getTradingPeriod().printTradingPeriod();
    }

    private static void testDbConnection() {
        CandlestickDao candlestickDao = new CandlestickDao();
        List<Candlestick> candlesticks = candlestickDao.getAllRows();
        candlesticks.forEach(System.out::println);
    }

    public static void testAddCandlestickToDb() throws Exception {
        Stock aak = new Stock("HM-B.ST", JsonConstants.ONE_YEAR, ONE_DAY);
        CandlestickDao candlestickDao = new CandlestickDao();
        candlestickDao.addRows(aak.getSymbol(), aak.getTradingPeriod().getCandlesticks());
    }
    public static void scrapeAllInfo() {
        LargeCapScraper largeCapScraper = new LargeCapScraper();
        MidCapScraper midCapScraper = new MidCapScraper();
        FirstNorthScraper firstNorthScraper = new FirstNorthScraper();
        SmallCapScraper smallCapScraper = new SmallCapScraper();
    }
}