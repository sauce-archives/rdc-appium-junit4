# RDC Appium Junit4 [![Build Status](https://travis-ci.org/saucelabs/rdc-appium-junit4.svg?branch=master)](https://travis-ci.org/saucelabs/rdc-appium-junit4)

Sauce Labs **R**eal **D**evice **C**loud Appium Junit4 Client Library for:

* Running Appium test suites.
* Updating test status on RDC.



## How to run an Appium test suite?
1. Create a test suite in your project on RDC.
2. Get the project API key and suite Id and put them in the annotation like the following example:

```java
@RunWith(RdcAppiumSuite.class)
@Rdc(apiKey = "your project API key goes here", suiteId = 3)
public class RdcAppiumSuiteWatcherTest {

	@Rule
	public RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		// Add these capabilities
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, watcher.getApiKey());
		capabilities.setCapability(RdcCapabilities.TEST_REPORT_ID, watcher.getTestReportId());

		// Initializing Appium driver and setting the watcher
		driver = new AndroidDriver(watcher.getAppiumEndpointUrl(), capabilities);
		watcher.setRemoteWebDriver(driver);
	}

	@Test
	public void testIt() {
		// driver.testAllTheThings();
	}

	// No need to close the driver. The library does that automatically.
}
```




## How to update test status on RDC?
I you want to see *SUCCESS* or *FAILURE* instead of *UNKNOWN* in your test reports on RDC website, you need to use the `RdcTestResultWatcher` like the following:
```java
public class RdcTestResultWatcherTest {

	@Rule
	public RdcTestResultWatcher watcher = new RdcTestResultWatcher();
	private AppiumDriver driver;

	@Before
	public void setup() {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(RdcCapabilities.API_KEY, "Your project API key");

		// Initializing Appium driver and setting the watcher
		driver = new AndroidDriver(RdcEndpoints.EU_ENDPOINT, capabilities);
		watcher.setRemoteWebDriver(driver);
	}

	@Test
	public void aTestThatUpdatesHisTestResultAfterwards() {
		// driver.testAllTheThings();
	}

	// No need to close the driver. The library does that automatically.
}
```
