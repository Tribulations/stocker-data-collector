package stocker.data.fetchers.wScrape;

/**
 * A class containing different constants that are used when running the web scrape on avanza.se.
 */
public final class ScrapeConstants {
    private ScrapeConstants() { throw new IllegalStateException("Utility class"); }

    /** The x-paths of different buttons that has to be clicked */
    public static final String SHOW_STOCK_LISTS_BTN_XPATH = "/html/body/main/section/div/main/div/div/div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/button/span[2]";
    public static final String LARGE_CAP_BTN_XPATH = "/html/body/main/section/div/main/div/div/div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/ul/li/ul/li[3]";
    public static final String MID_CAP_BTN_XPATH = "/html/body/main/section/div/main/div/div/div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/ul/li/ul/li[4]";
    public static final String SMALL_CAP_BTN_XPATH = "/html/body/main/section/div/main/div/div/div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/ul/li/ul/li[8]";
    public static final String FIRST_NORTH_BTN_XPATH = "/html/body/main/section/div/main/div/div/div[5]/div/div[2]/div/div[6]/div[2]/div/div[2]/ul/li/ul/li[10]";
    public static final String COOKIE_BTN_XPATH = "/html/body/div[2]/div/div/div/button[1]";
    public static final String SYMBOL_NAME_XPATH = "/html/body/aza-app/aza-shell/div/main/div/aza-stock/aza-subpage/div/div/aza-pull-to-refresh/div/div/aza-page-container/aza-page-container-inset/section/div[1]/div[3]/div[2]/div/aza-instrument-expander-card[1]/mint-card/aza-expansion-panel/div/div[2]/aza-expansion-panel-body/div/div[1]/div/mint-pair-list[1]/mint-pair-list-row[2]/mint-pair-list-value";

    /** class name of the 'visa fler' stock button */
    public static final String FETCH_MORE_BTN = "fetchMoreButton";
    /** The url of the web page */
    public static final String AVANZA_STOCK_LIST_URL = "https://www.avanza.se/aktier/lista.html/";
    public static final String AVANZA_ABOUT_STOCK_URL = "https://www.avanza.se/aktier/om-aktien.html/";

    /** web driver constants */
    public static String FIRE_FOX_WEB_DRIVER = "webdriver.gecko.driver";
    public static String FIRE_FOX_WEB_DRIVER_PATH = "src/main/resources/geckodriver";

    /** Timeout constants */
    public static final int LONG_TIMEOUT = 2000;
    public static final int SHORT_TIMEOUT = 500;

    /** Container rows holding stock names and id's */
    public static final String STOCK_ROW_ELEMENTS_CLASS_NAME ="orderbookName";
    public static final String STOCK_ROW_ELEMENTS_ID = "td ul li a";

    /** Map keys for different stock lists */
    public static final String LARGE_CAP = "LARGE_CAP";
    public static final String MID_CAP = "MID_CAP";
    public static final String SMALL_CAP = "SMALL_CAP";
    public static final String FIRST_NORTH = "FIRST_NORTH";
}
