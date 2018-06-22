package com.saucelabs.rdc;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.remote.DesiredCapabilities;

@Ignore
@RunWith(RdcAppiumSuite.class)
@Rdc(apiKey = "Your project API key goes here", suiteId = 123)
public class RdcAppiumSuiteWatcherTest {

	@Rule
	public RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, watcher.getApiKey());
		capabilities.setCapability(RdcCapabilities.TEST_REPORT_ID, watcher.getTestReportId());

		driver = new AndroidDriver(watcher.getAppiumEndpointUrl(), capabilities);
		watcher.setRemoteWebDriver(driver);

		printUsefulLinks();
	}

	@Test
	public void getPageSource() {
		System.out.println(driver.getPageSource());
	}

	private void printUsefulLinks() {
		System.out.println("Live view: " + driver.getCapabilities().getCapability(RdcCapabilities.TEST_LIVE_VIEW_URL));
		System.out.println("Test report: " + driver.getCapabilities().getCapability(RdcCapabilities.TEST_REPORT_URL));
	}
}

