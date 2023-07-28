package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import stocker.support.StockAppLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base web scraper defining shared fields and methods. The subclasses are used to scrape the names, id's, and short names/symbols for the stocks on the different lists which exist on the Swedish Nasdaq OMX. The different lists which can be scraped for this data are Large, Mid and Small Cap as well as First North.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-26
 */
public abstract class BaseScraper {
    private  final List<StockInfo> stockInfoList;
    protected static WebDriver driver;
    protected static WebDriverWait explicitWait;
    private static Set<Cookie> cookieSet = null;

    /**
     * Initializes member variables, and calls {@link #initWebDriver()}.
     */
    protected BaseScraper() {
        initWebDriver();
        stockInfoList = new ArrayList<>();
    }

    /**
     * Initializes {@link #driver}, accepts GDPR by clicking button, and stores/adds cookies.
     */
    private void initWebDriver() {
        // path to the GeckoDriver executable
        System.setProperty(Constants.FIRE_FOX_WEB_DRIVER, Constants.FIRE_FOX_WEB_DRIVER_PATH);
        driver = new FirefoxDriver();
        explicitWait = new WebDriverWait(driver, 4);

        // save cookies and remove popup
        if (cookieSet == null) {
            driver.get(Constants.AVANZA_STOCK_LIST_URL);
            try {
                Thread.sleep(Constants.LONG_TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            WebElement cookieBtn = driver.findElement(By.xpath(Constants.COOKIE_BTN_XPATH));
            cookieBtn.click();
            cookieSet = driver.manage().getCookies();
        } else { // load cookies
            driver.get(Constants.AVANZA_STOCK_LIST_URL);
            cookieSet.forEach( cookie -> {
                driver.manage().addCookie(cookie);
            });
        }
    }

    /**
     * The method used to scrape the stock names and their Avanza id's, and then calls
     * {@link #scrapeStockSymbol()} to retrieve the stock symbols.
     */
    public abstract void scrapeStockInfo();

    /**
     * Used internally to get each stocks symbol/short name after the full name and id has been retrieved.
     * This method is only called from {@link #scrapeStockInfo()} which is defined in subclasses.
     */
    protected void scrapeStockSymbol() {
        this.getStockInfoList().forEach(stockInfo -> { // Todo use a for loop to get rid of the catch block in this forEach. this way we only need one try catch block
            try {
                final String formattedStockName = stockInfo.getName().replace(" ", "-");
                final String symbolStockUrl = String.format("%s%s/%s", Constants.AVANZA_ABOUT_STOCK_URL,  stockInfo.getId(), formattedStockName);
                driver.get(symbolStockUrl);
                Thread.sleep(6500);
                WebElement symbolNameElement = driver.findElement(By.xpath(Constants.SYMBOL_NAME_XPATH));
                stockInfo.setSymbol(symbolNameElement.getText().replace(" ", "-"));
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
    * Method used to interact with needed elements on Avanza.se in order to remove the Large Cap stocks from the stock list. This is necessary when we want to scrape other stock lists than Large cap because large cap is displayed by default.
    */
    protected void removeLargeCapStockFromList(WebDriver driver) {
        WebElement largeCapListBtn = driver.findElement(By.xpath(Constants.LARGE_CAP_BTN_XPATH));
        largeCapListBtn.click();
    }

    /**
     * Adds all the scraped stock names, id's but NOT the symbol to a StockInfo objects to a list.
     * @param stockNameOffset the offset/difference between the stock name rows and stock avanza id row elements on Avanza.se
     */
    protected void createStockInfo(final int stockNameOffset) {
        try {
            Thread.sleep(Constants.LONG_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
        }
        // get the rows containing stock names and id rows and add to variables
        List<WebElement> stockNameContainers = driver.findElements(By.className(
                Constants.STOCK_ROWS_CLASS_NAME_ELEMENTS));
        List<WebElement> stockIdContainers = driver.findElements(By.cssSelector(
                Constants.STOCK_ROWS_ID_ELEMENTS));
        for (int i = 0; i < stockIdContainers.size(); ++i) {
            // get the name
            final String stockName = stockNameContainers.get(i + stockNameOffset).getText();
            // get the id
            final List<String> idHrefs = List.of(stockIdContainers.get(i).getAttribute("href").split("/"));
            final String stockId = idHrefs.get(idHrefs.size() - 1);
            stockInfoList.add(new StockInfo(stockName, stockId));
        }
    }

    /**
    * Writes info about all stocks from a stock list to file.
    */
    protected void writeStocksToFile() {
        try {
            String fileName = String.valueOf(this.getClass());
            FileWriter fileWriter = new FileWriter("src/main/resources/" + fileName + ".txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // write as csv to file
            stockInfoList.forEach(stockInfo -> {
                try {
                    bufferedWriter.write(String.format("%s,%s,%s%n", stockInfo.getName(), stockInfo.getId(), stockInfo.getSymbol()));
                } catch (IOException e) {
                    StockAppLogger.INSTANCE.logInfo(e.getMessage());
                    e.printStackTrace();
                }
            });

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            StockAppLogger.INSTANCE.logDebug(e.getMessage());
        }
    }

    /**
     * Returns the list containing objects with stock info, creating the list if necessary.
     * @return the list of StockInfo objects
     */
    public List<StockInfo> getStockInfoList() {
        if (stockInfoList.isEmpty()) {
            this.scrapeStockInfo();
            writeStocksToFile();
            driver.quit();
        }
        return stockInfoList;
    }
}

