package com.saucelabs.rdc;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.saucelabs.rdc.RdcCapabilities.API_KEY;

/**
 * An {@code RdcTestResultWatcher} updates the result of a test at Sauce Labs.
 * <p>Sauce Labs stores data about each test that you are executing on its Real
 * Device Cloud, e.g. request logs and screenshots. By default Sauce Labs does
 * not store the result of an Appium test. This is because the test result is
 * determined by the client that executes the test and therefore Sauce Labs
 * does not know about it.
 * <p>You can automatically update the test result by adding an
 * {@code RdcTestResultWatcher} rule to your test. Apart from adding the rule
 * you also have to call
 * {@link #setRemoteWebDriver(RemoteWebDriver) setRemoteWebDriver} because the
 * watcher needs to read the API key and the session from the
 * {@code AppiumDriver} so that it updates the right test. See the following
 * example.
 * <pre>public class YourTest {
 *     {@literal @Rule}
 *     public final RdcTestResultWatcher watcher = new RdcTestResultWatcher();
 *
 *     private AppiumDriver driver;
 *
 *     {@literal @Before}
 *     public void setup() {
 *         DesiredCapabilities capabilities = new DesiredCapabilities();
 *         capabilities.setCapability({@link RdcCapabilities#API_KEY}, "Your project API key");
 *
 *         driver = new AndroidDriver({@link RdcEndpoints#EU}, capabilities);
 *         watcher.setRemoteWebDriver(driver);
 *     }
 *
 *     {@literal @Test}
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
public class RdcTestResultWatcher implements TestRule {

	private static final boolean PASSED = true;

	private RemoteWebDriver webDriver;

	/**
	 * Set the WebDriver. {@code RdcTestResultWatcher} needs to read the API key
	 * and the session from the {@code AppiumDriver}.
	 * @param webDriver the WebDriver that is used for the test.
	 * @since 1.0.0
	 */
	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
					safeUpdateTestReport(PASSED);
				} catch (Throwable e) {
					safeUpdateTestReport(!PASSED);
					throw e;
				} finally {
					safeQuitWebDriver();
				}
			}
		};
	}

	private void safeUpdateTestReport(boolean passed) {
		if (webDriver != null) {
			try {
				updateTestReport(passed);
			} catch (Exception e) {
				System.err.println("Failed to update test report. Caused by:");
				e.printStackTrace(System.err);
			}
		}
	}

	private void updateTestReport(boolean passed) {
		new SauceLabsApi(apiToken())
			.updateTestReportStatus(webDriver.getSessionId(), passed);
	}

	private String apiToken() {
		return (String) webDriver.getCapabilities()
			.getCapability(API_KEY);
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
}
