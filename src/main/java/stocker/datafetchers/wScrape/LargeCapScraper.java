package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.IOException;

public class LargeCapScraper extends BaseScraper {

    public LargeCapScraper() throws InterruptedException {
        super();
    }

    @Override
    public void scrapeStockInfo() throws InterruptedException, IOException {
        // Open the webpage
        super.driver.get(AvanzaConstants.AVANZA_STOCK_LIST_URL);

        // Find the cookie button/ consent button and click it to remove it
//        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
//        WebElement cookieBtn = driver.findElement(By.xpath(AvanzaConstants.COOKIE_BTN_XPATH));
//        cookieBtn.click();

        // make the web browser show all large cap stock
        Thread.sleep(AvanzaConstants.LONG_TIMEOUT);
        WebElement fetchMoreBtn = driver.findElement(By.className(AvanzaConstants.FETCH_MORE_BTN));
        // we have to click the 'visa fler' stock button to show all stock on screen
        fetchMoreBtn.click();
        // add to variables
        // need to change to this becuase now we add the mid cap stock to the large cap key int the list todo
        createStockInfo(2);
        // get the symbol for each stock
        super.scrapeStockSymbol();
    }

}
