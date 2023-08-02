package stocker.datafetchers;

import stocker.database.CandlestickDao;
import stocker.stock.Stock;
import stocker.support.StockAppLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static stocker.datafetchers.wJson.JsonConstants.*;

/**
 * Class used to fetch data for many days for all stocks and add to db.
 */
public class MainDataFetcher {
    private final List<String> stockSymbols = new ArrayList<>();
    private final List<String> fileNames = Arrays.asList("src/main/resources/LargeCap.txt",
            "src/main/resources/MidCap.txt", "src/main/resources/SmallCap.txt");

    public MainDataFetcher() {
        initStockSymbolNames();
    }

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
        System.out.println(stockSymbols.size());
    }

    private void addNewPriceDataToDb() {
        List<Stock> stocks = new ArrayList<>();
        CandlestickDao candlestickDao = new CandlestickDao();

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol + ".ST", ONE_MONTH, ONE_DAY);
            stocks.add(stock);
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    public static void main(String... args) {

        MainDataFetcher mainDataFetcher = new MainDataFetcher();
        mainDataFetcher.addNewPriceDataToDb();
    }
}
