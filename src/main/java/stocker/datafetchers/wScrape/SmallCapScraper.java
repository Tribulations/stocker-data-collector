package stocker.datafetchers.wScrape;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SmallCapScraper extends BaseScraper {
    public SmallCapScraper() {
        super();
    }

    @Override
    public void scrapeStockInfo() {
        WebElement showStockListsBtn = driver.findElement(By.xpath(AvanzaConstants.SHOW_STOCK_LISTS_BTN_XPATH));
        explicitWait.until(ExpectedConditions.elementToBeClickable(showStockListsBtn));
        showStockListsBtn.click();
        removeLargeCapStockFromList(driver);
        clickShowSmallCap(driver);
        createStockInfo(1);
        scrapeStockSymbol();
    }

    protected void clickShowSmallCap(WebDriver driver) {
        WebElement smallCapListBtn = driver.findElement(By.xpath(AvanzaConstants.SMALL_CAP_BTN_XPATH));
        smallCapListBtn.click();
    }
}
