package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class MidCapScraper extends BaseScraper {
    public MidCapScraper() {
        super();
    }

    @Override
    public void scrapeStockInfo() {
        WebElement showStockListsBtn = driver.findElement(By.xpath(AvanzaConstants.SHOW_STOCK_LISTS_BTN_XPATH));
        explicitWait.until(ExpectedConditions.elementToBeClickable(showStockListsBtn));
        showStockListsBtn.click();
        removeLargeCapStockFromList(driver);
        clickShowMidCap(driver);
        WebElement fetchMoreBtn = driver.findElement(By.className(AvanzaConstants.FETCH_MORE_BTN));
        explicitWait.until(ExpectedConditions.elementToBeClickable(fetchMoreBtn));
        fetchMoreBtn.click();
        createStockInfo(2);
        scrapeStockSymbol();
    }

    protected void clickShowMidCap(WebDriver driver) {
        WebElement midCapListBtn = driver.findElement(By.xpath(AvanzaConstants.MID_CAP_BTN_XPATH));
        midCapListBtn.click();
    }
}
