package stocker.data.fetchers.wJson;

public final class JsonConstants {
    private JsonConstants() { throw new IllegalStateException("Utility class"); }

    public static final String CHART = "chart";
    public static final String RESULT = "result";

    // meta keys
    public static final String META = "meta";
    public static final String CURRENCY = "currency";
    public static final String SYMBOL = "symbol";
    public static final String EXCHANGE_NAME = "exchangeName";
    public static final String INSTRUMENT_TYPE = "instrumentType";
    public static final String FIRST_TRADE_DATE = "firstTradeDate";
    public static final String REGULAR_MARKET_TIME = "regularMarketTime";
    public static final String GMT_OFFSET = "gmtoffset";
    public static final String TIMEZONE = "timezone";
    public static final String EXCHANGE_TIMEZONE_NAME = "exchangeTimezoneName";
    public static final String REGULAR_MARKET_PRICE = "regularMarketPrice";
    public static final String HAS_PRE_POST_MARKET_DATA = "hasPrePostMarketData";
    public static final String CHART_PREVIOUS_CLOSE = "chartPreviousClose";
    public static final String PREVIOUS_CLOSE = "previousClose";
    public static final String SCALE = "scale";
    public static final String PRICE_HINT = "priceHint";
    public static final String CURRENT_TRADING_PERIOD = "currentTradingPeriod";
    public static final String PRE = "pre";
    public static final String START = "start";
    public static final String END = "end";
    public static final String REGULAR = "regular";
    public static final String POST = "post";
    public static final String TRADING_PERIODS = "tradingPeriods";
    public static final String DATA_GRANULARITY = "dataGranularity";
    public static final String RANGE = "range";
    public static final String VALID_RANGES = "validRanges";

    /** Valid ranges and intervals */
    public static final String ONE_MIN = "1m";
    public static final String FIVE_MIN = "5m";
    public static final String FIFTEEN_MIN = "15m";
    public static final String ONE_HOUR = "1h";
    public static final String ONE_WEEK = "1wk";
    public static final String ONE_DAY = "1d";
    public static final String FIVE_DAY = "5d";
    public static final String ONE_MONTH = "1mo";
    public static final String THREE_MONTHS = "3mo";
    public static final String SIX_MONTHS = "6mo";
    public static final String ONE_YEAR = "1y";
    public static final String TWO_YEAR = "2y";
    public static final String FIVE_YEARS = "5y";
    public static final String TEN_YEARS = "10y";
    public static final String YTD = "ytd";
    public static final String MAX = "max";

    /** Timestamp key */
    public static final String TIMESTAMP = "timestamp";

    /** indicator keys */
    public static final String INDICATORS = "indicators";
    public static final String QUOTE = "quote";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String LOW = "low";
    public static final String HIGH = "high";
    public static final String VOLUME = "volume";

    /**  Keys used when we get an error */
    public static final String ERROR = "error";
    public static final String CODE = "code";
    public static final String DESCRIPTION = "description";
}
