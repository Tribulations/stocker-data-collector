package stocker.model;

/**
 * Class storing a stock name, stock symbol name and, stock id.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-28
 */
public class StockInfo {
    private final String name;
    private final String id;
    private String symbol;

    /**
     * Public construction initializing member variables {@link #name} and {@link #id}.
     * @param name the stocks full name
     * @param id the id of the stock on Avanza
     */
    public StockInfo(final String name, final String id) {
        this.name = name;
        this.id = id;
        this.symbol = "";
    }

    /**
     * Public to string method
     * @return the stocks full name, id and short name/symbol if set.
     */
    @Override
    public String toString() {
        return String.format("%s, %s, %s", name, id, symbol);
    }

    /**
     * Public accessor.
     * @return the stocks name
     */
    public String getName() {
        return name;
    }

    /**
     * Public accessor.
     * @return the stocks id
     */
    public String getId() {
        return id;
    }

    /**
     * Public accessor.
     * @return the stocks short name/symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Public mutator used to set the stocks short name/symbol.
     * @param symbol the name to set the stocks short name/symbol to
     */
    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }
}
