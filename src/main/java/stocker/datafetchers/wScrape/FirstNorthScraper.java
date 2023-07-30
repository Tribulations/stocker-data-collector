package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Concrete scrape class used to scrape the names, id's, and short names/symbols for the stocks on First North.
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0 2023-07-28
 */
public class FirstNorthScraper extends BaseScraper {
    /**
     * Public construction simply calling super.
     */
    public FirstNorthScraper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scrapeStockInfo() {
        showStockListChanger();
        removeLargeCapStockFromList(driver);
        clickShowFirstNorth(driver);
        WebElement fetchMoreBtn = driver.findElement(By.className(ScrapeConstants.FETCH_MORE_BTN));
        final int fetchMoreBtnClickCount = 4;
        for (int i = 0; i < fetchMoreBtnClickCount; ++i) {
            explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn));
            fetchMoreBtn.click();
        }
        createStockInfo(2);
        scrapeStockSymbols();
    }

    /**
     * Method used internally to make the stock list visible.
     * @param driver the web driver
     */
    private void clickShowFirstNorth(WebDriver driver) {
        WebElement firstNorthListBtn = driver.findElement(By.xpath(ScrapeConstants.FIRST_NORTH_BTN_XPATH));
        firstNorthListBtn.click();
    }
}
