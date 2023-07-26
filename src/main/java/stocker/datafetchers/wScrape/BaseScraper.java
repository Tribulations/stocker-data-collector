package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class BaseScraper {
    private  final List<StockInfo> stockInfoList;
    protected static WebDriver driver;
    private static Set<Cookie> cookieSet = null;
//    private static FirefoxOptions firefoxOptions = null;

    // TODO make driver static and store cookies in it so we don't have to send a get request each time?
    /**
     * initializes members variables, init driver and store cookies for later use.
     */
    protected BaseScraper() throws InterruptedException {
        initWebDriver();
        stockInfoList = new ArrayList<>();
    }

    protected void initWebDriver() throws InterruptedException {
        // Set the path to the ChromeDriver executable
        System.setProperty(AvanzaConstants.FIRE_FOX_WEB_DRIVER, AvanzaConstants.FIRE_FOX_WEB_DRIVER_PATH);
        // Create a new instance of the Firefox driver
        driver = new FirefoxDriver();

        // either get the cookies and save or load and add cookies to the driver
        if (cookieSet == null) { // save the cookies
            driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);
            // TODO try and get the cookies so we dont need to http get request for getting each stock list when window is reopened
            // Find the cookie button/ consent button and click it to remove it
            Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
            WebElement cookieBtn = driver.findElement(By.xpath(AvanzaConstants.COOKIE_BTN_XPATH));
            cookieBtn.click();
            cookieSet = driver.manage().getCookies();
        } else { // load previously saved cookies
            // add cookies to the web driver
            driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);
            cookieSet.forEach( cookie -> {
                driver.manage().addCookie(cookie);
            });
        }
    }

    public abstract void scrapeStockInfo() throws InterruptedException, IOException;

    /**
     * used to get each stocks symbol/short name after the full name and id has been retrieved,
     * @throws InterruptedException
     * @throws IOException
     */
    protected void scrapeStockSymbol() throws InterruptedException, IOException {
        if (this.getStockInfoList().isEmpty()) { // todo don't need this check anymore cause the stockInfo list is never empty here?
            // todo maybe throw other exception
            throw new InterruptedException(
                    "No prior stock objects exist. Stock names and id has to be scraped first");
        } else {
//            this.initWebDriver(); // not needed anmore todo?

            // todo. go to the web page for each stock, scrape the symbol, and add it to the stock llst
            // the id and stock name is part of the url so loop over al stockInfo objects and create url
            this.getStockInfoList().forEach(stockInfo -> {
                final String formattedStockName = stockInfo.getName().replace(" ", "-");
                final String symbolStockUrl = String.format("%s%s/%s",AvanzaConstants.AVANZA_ABOUT_STOCK_URL,  stockInfo.getId(), formattedStockName);
                try {
                    driver.get(symbolStockUrl);
                    Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
                    Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
                    Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
                    Thread.sleep(AvanzaConstants.SHORT_TIMEOUT);
                    WebElement symbolNameElement = driver.findElement(By.xpath(AvanzaConstants.SYMBOL_NAME_XPATH));
                    stockInfo.setSymbol(symbolNameElement.getText().replace(" ", "-"));
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); // todo bad practice
                }
            });
        }
    }

    protected void removeLargeCapStockFromList(WebDriver driver) throws InterruptedException {
        WebElement largeCapListBtn = driver.findElement(By.xpath(AvanzaConstants.LARGE_CAP_BTN_XPATH));
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        largeCapListBtn.click();
    }

    /**
     * Adds all the scraped stock names, id's but NOT the symbol to a StockInfo objects to a list.
     * @param stockNameOffset the offset/difference between the stock name rows and stock avanza id rows
     * @throws InterruptedException when that exception occurs
     */
    protected void createStockInfo(final int stockNameOffset) throws InterruptedException {
        // new line
//        System.out.println();
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        // get the rows containing stock names and id rows and add to variables
        List<WebElement> stockNameContainers = driver.findElements(By.className(
                AvanzaConstants.STOCK_ROWS_CLASS_NAME_ELEMENTS));
        List<WebElement> stockIdContainers = driver.findElements(By.cssSelector(
                AvanzaConstants.STOCK_ROWS_ID_ELEMENTS));
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);

        // prints  and adds the stock name and its id on avanza.se to member variable map todo old commenet change!
        for (int i = 0; i < stockIdContainers.size(); ++i) {
            // get the name
            final String stockName = stockNameContainers.get(i + stockNameOffset).getText();
            // get the id
            final List<String> idHrefs = List.of(stockIdContainers.get(i).getAttribute("href").split("/"));
            final String stockId = idHrefs.get(idHrefs.size() - 1);
            // add to member variable list
            stockInfoList.add(new StockInfo(stockName, stockId)); // todo last arg to be the symbol
//            System.out.println(
//                    stockName + ", id: " + stockId);
        }
        System.out.println(stockIdContainers.size());
    }

    /**
     * use this one to quit the driver after all stock has been scraped to just use one get reqeust
     */
//    public void quitDriver(){
//        driver.quit();
//    }
    protected void writeStocksToFile() throws IOException {
        String fileName = String.valueOf(this.getClass());
        FileWriter fileWriter = new FileWriter("src/main/resources/" + fileName + ".txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // write as csv to file
        stockInfoList.forEach(stockInfo -> {
            try {
                bufferedWriter.write(String.format("%s,%s,%s%n", stockInfo.getName(), stockInfo.getId(), stockInfo.getSymbol()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        bufferedWriter.close();
        fileWriter.close();
    }

    // TODO I have to catch these exxception some good place ot keep declarin methos as throws anymore haha
    public List<StockInfo> getStockInfoList() throws InterruptedException, IOException {
        if (stockInfoList.isEmpty()) {
            this.scrapeStockInfo();
            writeStocksToFile();
            driver.quit();
        }
        return stockInfoList;
    }
}

