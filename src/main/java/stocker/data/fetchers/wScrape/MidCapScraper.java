package stocker.data.fetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Concrete scrape class used to scrape the names, id's, and short names/symbols for the stocks on Mid-cap.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-28
 */
public class MidCapScraper extends BaseScraper {
    /**
     * Public construction simply calling super.
     */
    public MidCapScraper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrapeStockInfo() {
        showStockListChanger();
        removeLargeCapStockFromList(driver);
        clickShowMidCap(driver);
        WebElement fetchMoreBtn = driver.findElement(By.className(ScrapeConstants.FETCH_MORE_BTN));
        explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn));
        fetchMoreBtn.click();
        createStockInfo(2);
        scrapeStockSymbols();
    }

    /**
     * Method used internally to make the stock list visible.
     * @param driver the web driver
     */
    private void clickShowMidCap(WebDriver driver) {
        WebElement midCapListBtn = driver.findElement(By.xpath(ScrapeConstants.MID_CAP_BTN_XPATH));
        midCapListBtn.click();
    }
}
