package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;

public class FirstNorthScraper extends BaseScraper {
    public FirstNorthScraper() throws InterruptedException {
        super();
    }

    @Override
    public void scrapeStockInfo() throws InterruptedException, IOException {
        // Open the webpage
        driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);

        // Find the cookie button/ consent button and click it to remove it
//        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
//        WebElement cookieBtn = driver.findElement(By.xpath(AvanzaConstants.COOKIE_BTN_XPATH));
//        cookieBtn.click();

        // make the web browser show all first north stocks by first removing large cap stock from the list
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
        // add to variables
        // need to change to this becuase now we add the mid cap stock to the large cap key int the list todo
        createStockInfo(2);
        super.scrapeStockSymbol();
    }

    private void clickShowFirstNorth(WebDriver driver) throws InterruptedException {
        WebElement firstNorthListBtn = driver.findElement(By.xpath(AvanzaConstants.FIRST_NORTH_BTN_XPATH));
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        firstNorthListBtn.click();
    }

}
