package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FirstNorthScraper extends BaseScraper {
    public FirstNorthScraper() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scrapeStockInfo() {
        WebElement showStockListsBtn = driver.findElement(By.xpath(AvanzaConstants.SHOW_STOCK_LISTS_BTN_XPATH));
        explicitWait.until(ExpectedConditions.elementToBeClickable(showStockListsBtn));
        showStockListsBtn.click();
        removeLargeCapStockFromList(driver);
        clickShowFirstNorth(driver);
        WebElement fetchMoreBtn = driver.findElement(By.className(AvanzaConstants.FETCH_MORE_BTN));
        final int fetchMoreBtnClickCount = 4;
        for (int i = 0; i < fetchMoreBtnClickCount; ++i) {
            explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn));
            fetchMoreBtn.click();
        }
        createStockInfo(2);
        scrapeStockSymbol();
    }

    private void clickShowFirstNorth(WebDriver driver) {
        WebElement firstNorthListBtn = driver.findElement(By.xpath(AvanzaConstants.FIRST_NORTH_BTN_XPATH));
        firstNorthListBtn.click();
    }
}
