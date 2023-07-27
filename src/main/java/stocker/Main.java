package stocker;

import stocker.datafetchers.wJson.StockAppLogger;
import stocker.datafetchers.wScrape.*;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
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