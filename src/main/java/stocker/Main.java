package stocker;
// TODO add package.info files to all packages
import stocker.database.CandlestickDao;
import stocker.stock.Candlestick;
import stocker.stock.StockInfo;
import stocker.support.StockAppLogger;
import stocker.datafetchers.wScrape.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        testDbConnection();
    }

    private static void testDbConnection() {
        CandlestickDao candlestickDao = new CandlestickDao();
        List<Candlestick> candlesticks = candlestickDao.getAllRows();
        candlesticks.forEach(System.out::println);
    }

    public static void scrapeAllInfo() {
        LargeCapScraper largeCapScraper = new LargeCapScraper();
        MidCapScraper midCapScraper = new MidCapScraper();
        FirstNorthScraper firstNorthScraper = new FirstNorthScraper();
        SmallCapScraper smallCapScraper = new SmallCapScraper();
    }
}