package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcTestParser;
import com.saucelabs.rdc.helper.reporter.SuiteReporter;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.OptionalInt;

/**
 * {@code RdcAppiumSuiteWatcher} updates the result of a test at Sauce Labs and
 * closes the {@code WebDriver} at the end of the test. It is designed for
 * tests that are executed by the {@link RdcAppiumSuite} runner.
 * <p>Sauce Labs stores data about each test that you are executing on its Real
 * Device Cloud, e.g. request logs and screenshots. By default Sauce Labs does
 * not store the result of an Appium test. This is because the test result is
 * determined by the client that executes the test and therefore Sauce Labs
 * does not know about it.
 * <p>When your test is run by {@link RdcAppiumSuite} runner then you can
 * automatically update the test result by adding an
 * {@code RdcAppiumSuiteWatcher} rule to your test. Apart from adding the rule
 * you also have to call
 * {@link #setRemoteWebDriver(RemoteWebDriver) setRemoteWebDriver} because the
 * watcher needs to read the session from the {@code AppiumDriver} so that it
 * updates the right test. See the following example.
 * <pre>  {@literal @}RunWith(RdcAppiumSuite.class)
 * {@link Rdc @Rdc}(suiteId = 42)
 * public class YourTest {
 *    {@literal @Rule}
 *     public final RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();
 *
 *     private AppiumDriver driver;
 *
 *    {@literal @Before}
 *     public void setup() {
 *         DesiredCapabilities capabilities = new DesiredCapabilities();
 *         capabilities.setCapability({@link RdcCapabilities#API_KEY}, watcher.getApiKey());
 *         capabilities.setCapability({@link RdcCapabilities#TEST_REPORT_ID}, watcher.getTestReportId());
 *
 *         driver = new AndroidDriver(watcher.getAppiumEndpointUrl(), capabilities);
 *         watcher.setRemoteWebDriver(driver);
 *     }
 *
 *    {@literal @Test}
 *     public void yourTest() {
 *         ... //your test code
 *     }
 * }
 * </pre>
 * <p>{@code RdcTestResultWatcher} quits the WebDriver at the end of the test. You
 * don't have to do it in an {@literal @After} method anymore.
 *
 * @since 1.0.0
 */
public class RdcAppiumSuiteWatcher implements TestRule {

	private static final boolean PASSED = true;

	private String apiKey;
	private RdcTest test;
	private boolean isLocalTest;
	private URL appiumUrl;
	private URL apiUrl;

	private SuiteReporter reporter;
	private SuiteReport suiteReport;
	private RemoteWebDriver webDriver;

	/**
	 * Set the WebDriver. {@code RdcAppiumSuiteWatcher} needs to read the API key
	 * and the session from the {@code AppiumDriver}.
	 * @param webDriver the WebDriver that is used for the test.
	 * @since 1.0.0
	 */
	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
		reporter.setRemoteWebDriver(webDriver);
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					test = RdcTestParser.from(description);
					base.evaluate();
					safeUpdateTestReport(PASSED, description);
				} catch (Throwable e) {
					safeUpdateTestReport(!PASSED, description);
					throw e;
				} finally {
					safeQuitWebDriver();
				}
			}
		};
	}

	private void safeUpdateTestReport(boolean passed, Description description) {
		if (webDriver != null) {
			try {
				updateTestReport(passed, description);
			} catch (Exception e) {
				System.err.println(
					"Failed to update test report. Caused by "
						+ e.getLocalizedMessage());
			}
		}
	}

	private void updateTestReport(boolean passed, Description description) {
		RdcTest test = RdcTestParser.from(description);
		reporter.processAndReportResult(passed, test, apiUrl);
	}

	private void safeQuitWebDriver() {
		if (webDriver != null) {
			try {
				webDriver.quit();
			} catch (Exception e) {
				System.err.println(
					"Failed to quit WebDriver. Caused by "
						+ e.getLocalizedMessage());
			}
		}
	}

	public void configure(String apiKey, long suiteId, SuiteReport suiteReport, boolean isLocalTest, URL appiumUrl, URL apiUrl) {
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
		this.appiumUrl = appiumUrl;
		this.isLocalTest = isLocalTest;
		this.reporter = new SuiteReporter(suiteId, suiteReport);
		this.suiteReport = suiteReport;
	}

	/**
	 * Returns the ID of the Sauce Labs' test report of this test. This ID has
	 * to be sent as part of {@code DesiredCapabilities}. It returns
	 * {@code null} when the test runs locally.
	 * <pre>capabilities.setCapability({@link RdcCapabilities#TEST_REPORT_ID}, watcher.getTestReportId());</pre>
	 * @return the ID of the Sauce Labs' test report of this test or
	 * {@code null} when the test runs locally.
	 */
	public String getTestReportId() {
		if (suiteReport == null) {
			return null;
		} else {
			OptionalInt id = suiteReport.getTestReportId(test);
			if (id.isPresent()) {
				return Integer.toString(id.getAsInt());
			} else {
				throw new IllegalStateException("test report not present");
			}
		}
	}

	public String getTestDeviceId() {
		return suiteReport.getTestDeviceId(test)
				.orElseThrow(() -> new IllegalStateException("test device not present"));
	}

	/**
	 * Returns the API key that was configured for the {@link RdcAppiumSuite}
	 * runner that executes the tests. The API key has to be sent as part of
	 * {@code DesiredCapabilities}.
	 * <pre>capabilities.setCapability({@link RdcCapabilities#API_KEY}, watcher.getApiKey());</pre>
	 * @return the API key that was configured for the {@link RdcAppiumSuite}
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * Returns the remote address for the {@code AppiumDriver}.
	 * @return the remote address for the {@code AppiumDriver}.
	 */
	public URL getAppiumEndpointUrl() {
		if (isLocalTest) {
			try {
				return new URL("http://0.0.0.0:4723/wd/hub");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			return appiumUrl;
		}
	}
}
