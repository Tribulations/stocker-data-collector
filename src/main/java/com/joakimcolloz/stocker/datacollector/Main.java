package com.joakimcolloz.stocker.datacollector;

import com.joakimcolloz.stocker.datacollector.data.StockDataService;
import com.joakimcolloz.stocker.datacollector.database.DatabaseConfig;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import com.joakimcolloz.stocker.datacollector.utils.StockReader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Main class of the program.
 *
 * @author Joakim Colloz
 * @version 1.1
 */
public class Main {
    public static void main(String... args) {
        final DatabaseManager databaseManager = new DatabaseManager(new DatabaseConfig());
        databaseManager.initialize();

        ArrayList<String> stockList;

        final StockDataService stockDataService = new StockDataService();
        try {
            stockList = StockReader.readStockNamesFromResource("largecap.txt");
        } catch (IOException e) {
            System.out.println(e.getMessage()); // TODO temp logging improve!
            throw new RuntimeException(e);
        }

        stockDataService.addPriceDataToDb(stockList, Range.ONE_DAY, Interval.ONE_DAY);
    }
}
