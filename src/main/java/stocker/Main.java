package stocker;
// TODO add package.info files to all packages
import stocker.database.CandlestickDao;
import stocker.datafetchers.wJson.JsonConstants;
import stocker.datafetchers.wJson.StockDataFetcher;
import stocker.stock.Candlestick;
import stocker.stock.Stock;
import stocker.stock.StockInfo;
import stocker.support.StockAppLogger;
import stocker.datafetchers.wScrape.*;

import java.util.List;

import static stocker.datafetchers.wJson.JsonConstants.ONE_DAY;

public class Main {
    public static void main(String[] args) throws Exception {
        testGetSingleCandleStick();
    }

    private static void testGetSingleCandleStick() throws Exception {
        Stock aak = new Stock("AAK.ST", ONE_DAY, ONE_DAY);
        aak.getTradingPeriod().getCandlesticks().forEach(candlestick -> {
            System.out.println(candlestick.getTimestamp());
            System.out.println(candlestick);
        });
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