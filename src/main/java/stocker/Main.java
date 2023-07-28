package stocker;

import stocker.support.StockAppLogger;
import stocker.datafetchers.wScrape.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        StockAppLogger.INSTANCE.turnOffDebugLogging();
        scrapeAllInfo();
    }

    public static void scrapeAllInfo() {
        SmallCapScraper smallCapScraper = new SmallCapScraper();
        List<StockInfo> list2 = smallCapScraper.getStockInfoList();

        MidCapScraper midCapScraper = new MidCapScraper();
        List<StockInfo> list3 = midCapScraper.getStockInfoList();

        LargeCapScraper largeCapScraper = new LargeCapScraper();
        List<StockInfo> list4 = largeCapScraper.getStockInfoList();

        FirstNorthScraper firstNorthScraper = new FirstNorthScraper();
        List<StockInfo> list = firstNorthScraper.getStockInfoList();
    }
}