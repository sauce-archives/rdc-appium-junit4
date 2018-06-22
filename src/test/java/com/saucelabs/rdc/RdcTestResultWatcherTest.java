package com.saucelabs.rdc;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Ignore
public class RdcTestResultWatcherTest {

	@Rule
	public RdcTestResultWatcher watcher = new RdcTestResultWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, "Your project API key goes here");

		driver = new AndroidDriver(RdcEndpoints.EU_ENDPOINT, capabilities);
		watcher.setRemoteWebDriver(driver);

		printUsefulLinks();
	}

	@Test
	public void updateTestResultsOnSauceLabsRdcAfterCalculatorTest() {
		MobileElement buttonTwo = (MobileElement) driver.findElementById("net.ludeke.calculator:id/digit2");
		MobileElement buttonPlus = (MobileElement) driver.findElementById("net.ludeke.calculator:id/plus");
		MobileElement buttonEquals = (MobileElement) driver.findElementById("net.ludeke.calculator:id/equal");
		MobileElement resultField = (MobileElement) driver.findElementByXPath("//android.widget.EditText[1]");

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.textToBePresentInElement(resultField, "4"));
	}

	private void printUsefulLinks() {
		System.out.println("Live view: " + driver.getCapabilities().getCapability(RdcCapabilities.TEST_LIVE_VIEW_URL));
		System.out.println("Test report: " + driver.getCapabilities().getCapability(RdcCapabilities.TEST_REPORT_URL));
	}

}