package stocker.datafetchers.wScrape;

/**
 * Class storing a stock name, stock symbol name and, stock id.
 */
public class StockInfo {
    private final String name;
    private final String id;
    private String symbol;

    public StockInfo(String name, String id) {
        this.name = name;
        this.id = id;
        this.symbol = "";
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", name, id, symbol);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }
}
