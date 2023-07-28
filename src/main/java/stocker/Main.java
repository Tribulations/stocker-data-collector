package stocker;

import stocker.stock.StockInfo;
import stocker.support.StockAppLogger;
import stocker.datafetchers.wScrape.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        StockAppLogger.INSTANCE.turnOffDebugLogging();
        scrapeAllInfo();
    }

    public static void scrapeAllInfo() {
        LargeCapScraper largeCapScraper = new LargeCapScraper();
        MidCapScraper midCapScraper = new MidCapScraper();
        FirstNorthScraper firstNorthScraper = new FirstNorthScraper();
        SmallCapScraper smallCapScraper = new SmallCapScraper();
    }
}