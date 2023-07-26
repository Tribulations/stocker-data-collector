package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;

public class SmallCapScraper extends BaseScraper {
    public SmallCapScraper() throws InterruptedException {
        super();
    }

    @Override
    public void scrapeStockInfo() throws InterruptedException, IOException {
        // Open the webpage
//        driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);

        // Find the cookie button/ consent button and click it to remove it
//        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
//        WebElement cookieBtn = driver.findElement(By.xpath(AvanzaConstants.COOKIE_BTN_XPATH));
//        cookieBtn.click();

        // make the web browser show all first north stocks by first removing large cap
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        WebElement showStockListsBtn = driver.findElement(By.xpath(AvanzaConstants.SHOW_STOCK_LISTS_BTN_XPATH));
        showStockListsBtn.click();
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        // remove large cap stocks from showing
        super.removeLargeCapStockFromList(driver);
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        // show small cap stock
        clickShowSmallCap(driver);
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        // put all small cap into variable

        // add to variables
        // need to change to this becuase now we add the mid cap stock to the large cap key int the list todo
        createStockInfo(1);
        // Close the browser/ or refreseh is better? todo do we even need a refresh here?
//        driver.navigate().refresh();
        super.scrapeStockSymbol();
//        driver.quit(); // TODO we should maybe just refresh here and then use quitDriver() after all stock has een scraped? That way we just have to do one get request
    }

    protected void clickShowSmallCap(WebDriver driver) throws InterruptedException {
        WebElement smallCapListBtn = driver.findElement(By.xpath(AvanzaConstants.SMALL_CAP_BTN_XPATH));
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        smallCapListBtn.click();
    }

}
