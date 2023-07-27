package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;

public class FirstNorthScraper extends BaseScraper {
    public FirstNorthScraper() throws InterruptedException {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrapeStockInfo() {
        // Open the webpage
        driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);
        // make the web browser show all first north stocks by first removing large cap stock from the list
        try {
            Thread.sleep(AvanzaConstants.LONG_TIMEOUT);

            WebElement showStockListsBtn = driver.findElement(By.xpath(AvanzaConstants.SHOW_STOCK_LISTS_BTN_XPATH));
            showStockListsBtn.click();
            Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
            // remove large cap stocks from showing
            removeLargeCapStockFromList(driver);
            Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
            // show first north stock
            clickShowFirstNorth(driver);
            Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
            // put all small cap into variable
            WebElement fetchMoreBtn = driver.findElement(By.className(AvanzaConstants.FETCH_MORE_BTN));
            // we have to click the 'visa fler' stock button to show all stock on screen
            final int fetchMoreBtnClickCount = 4;
            for (int i = 0; i < fetchMoreBtnClickCount; ++i) {
                fetchMoreBtn.click();
            }
            // add stock info to member field in base class
            createStockInfo(2);
            super.scrapeStockSymbol();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void clickShowFirstNorth(WebDriver driver) throws InterruptedException {
        WebElement firstNorthListBtn = driver.findElement(By.xpath(AvanzaConstants.FIRST_NORTH_BTN_XPATH));
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        firstNorthListBtn.click();
    }

}
