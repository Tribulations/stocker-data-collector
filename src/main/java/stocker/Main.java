package stocker;

import stocker.datafetchers.wJson.Stock;
import stocker.datafetchers.wJson.StockAppLogger;
import stocker.datafetchers.wJson.StockDataFetcher;
import stocker.datafetchers.wJson.StockDataParser;

public class Main {
    public static void main(String[] args) throws Exception {

        StockAppLogger.INSTANCE.turnOffDebugLogging();

        Stock abb = new Stock("FABG.ST", "1d", "15m");
        abb.getTradingPeriod().printTradingPeriod();
    }
}