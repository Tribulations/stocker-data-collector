package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Concrete scrape class used to scrape the names, id's, and short names/symbols for the stocks on Small cap.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-28
 */
public class SmallCapScraper extends BaseScraper {
    /**
     * Public construction simply calling super.
     */
    public SmallCapScraper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrapeStockInfo() {
        showStockListChanger();
        removeLargeCapStockFromList(driver);
        clickShowSmallCap(driver);
        createStockInfo(1);
        scrapeStockSymbols();
    }

    /**
     * Method used internally to make the stock list visible.
     * @param driver the web driver
     */
    private void clickShowSmallCap(WebDriver driver) {
        WebElement smallCapListBtn = driver.findElement(By.xpath(ScrapeConstants.SMALL_CAP_BTN_XPATH));
        smallCapListBtn.click();
    }
}
