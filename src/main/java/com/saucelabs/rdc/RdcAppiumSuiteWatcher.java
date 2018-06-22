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

public class RdcAppiumSuiteWatcher implements TestRule {

	private static final boolean PASSED = true;

	private String apiKey;
	private RdcTest test;
	private boolean isLocalTest;
	private URL appiumUrl;
	private URL apiUrl;

	private SuiteReporter reporter;
	private RemoteWebDriver webDriver;

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
		reporter = new SuiteReporter(suiteId, suiteReport);
		setApiKey(apiKey);
		setIsLocalTest(isLocalTest);
		setAppiumUrl(appiumUrl);
		setApiUrl(apiUrl);
	}

	public void setAppiumUrl(URL appiumUrl) {
		this.appiumUrl = appiumUrl;
	}

	public void setApiUrl(URL apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setIsLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
	}

	public String getTestReportId() {
		OptionalInt id = reporter.suiteReport().getTestReportId(test);
		if (id.isPresent()) {
			return Integer.toString(id.getAsInt());
		} else {
			throw new IllegalStateException("test report not present");
		}
	}

	public String getTestDeviceId() {
		return reporter.suiteReport().getTestDeviceId(test)
				.orElseThrow(() -> new IllegalStateException("test device not present"));
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

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
