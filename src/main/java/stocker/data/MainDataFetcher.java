package stocker.data;

import stocker.database.CandlestickDao;
import stocker.representation.Stock;
import stocker.support.StockAppLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static stocker.data.fetchers.wJson.JsonConstants.*;

/**
 * Class used to fetch data for many days for all stocks and add to db.
 */
public class MainDataFetcher {
    private final List<String> stockSymbols = new ArrayList<>();
    private final List<String> fileNames = Arrays.asList("src/main/resources/LargeCap.txt",
            "src/main/resources/MidCap.txt", "src/main/resources/SmallCap.txt");

    /**
     * Public constructor calling internal method {@link #initStockSymbolNames()} to initialize member field
     * {@link #stockSymbols} storing the names/symbols of the stocks for which price data should be retrieved.
     */
    public MainDataFetcher() {
        initStockSymbolNames();
    }

    /**
     * TODO maybe we should remove the -PREF, -A stocks etc. directly when we scrape these names and just save the needed symbol names?
     * TODO As for now we have to remove the unneeded stock names everytime in this method which is unnecessary!
     */
    private void initStockSymbolNames() {
        // read file/s containing stock symbols
        // add only the wanted ones i.e. we don't want pref, a or c stock etc. to list.
        for (String fileName : fileNames) {
            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while (bufferedReader.ready()) {
                    final String[] readLine = bufferedReader.readLine().split(",");
                    final String symbol = readLine[2];
                    boolean addSymbol = !(symbol.contains("-PREF") || symbol.contains("-A") || symbol.contains("-D"));
                    if (addSymbol) {
                        stockSymbols.add(symbol);
                    }
                }
            } catch (IOException e) {
                StockAppLogger.INSTANCE.logInfo(e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println(stockSymbols.size()); // debug
    }

    /**
     *
     */
    private void addLatest1dPriceDataToDb() {
        List<Stock> stocks = new ArrayList<>();
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol + marketSuffix, ONE_DAY, ONE_DAY);
            stocks.add(stock); // todo maybe only use a Stock as parameter to addRow()/s
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    private void addMultipleOlder1dPriceDataToDb(final String range) {
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol + marketSuffix, range, ONE_DAY);
            candlestickDao.addMultipleOlderRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    public static void main(String... args) {
        MainDataFetcher mainDataFetcher = new MainDataFetcher();
//        mainDataFetcher.addLatest1dPriceDataToDb();
        mainDataFetcher.addMultipleOlder1dPriceDataToDb(ONE_MONTH);
    }
}
