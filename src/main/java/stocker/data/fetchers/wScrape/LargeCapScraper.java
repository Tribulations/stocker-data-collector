package stocker.data.fetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Concrete scrape class used to scrape the names, id's, and short names/symbols for the stocks on Large cap.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-28
 */
public class LargeCapScraper extends BaseScraper {
    /**
     * Public construction simply calling super.
     */
    public LargeCapScraper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrapeStockInfo() {
        try {
            Thread.sleep(ScrapeConstants.LONG_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement fetchMoreBtn = driver.findElement(By.className(ScrapeConstants.FETCH_MORE_BTN));
        explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn));
        fetchMoreBtn.click();
        createStockInfo(2);
        scrapeStockSymbols();
    }
}
