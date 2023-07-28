package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LargeCapScraper extends BaseScraper {

    public LargeCapScraper() {
        super();
    }

    @Override
    public void scrapeStockInfo() {
        try {
            Thread.sleep(Constants.LONG_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement fetchMoreBtn = driver.findElement(By.className(Constants.FETCH_MORE_BTN));
        explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn)); // TODO necessary?
        fetchMoreBtn.click();
        createStockInfo(2);
        scrapeStockSymbol();
    }
}
