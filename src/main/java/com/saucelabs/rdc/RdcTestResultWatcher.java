package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcEnvironmentVariables;
import com.saucelabs.rdc.helper.RdcListenerProvider;
import com.saucelabs.rdc.helper.reporter.ResultReporter;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static com.saucelabs.rdc.RdcEndpoints.DEFAULT_API_ENDPOINT;

public class RdcTestResultWatcher implements TestRule {

	private ResultReporter reporter;

	private RdcListenerProvider provider;

	public RdcTestResultWatcher() {
		provider = RdcListenerProvider.newInstance();
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
					reportStatus(true);
				} catch (Throwable e) {
					reportStatus(false);
					throw e;
				} finally {
					closeReporter();
				}
			}
		};
	}

	private void reportStatus(boolean status) {
		if (reporter != null) {
			try {
				reporter.createSuiteReportAndTestReport(status);
			} catch (Exception e) {
				System.err.println(
					"Failed to update test report. Caused by "
						+ e.getLocalizedMessage());
			}
		}
	}

	private void closeReporter() {
		if (reporter != null) {
			reporter.close();
		}
	}

	public void setRemoteWebDriver(RemoteWebDriver driver) {
		setApiUrl();
		provider.setDriver(driver);
		reporter = new ResultReporter(provider);
	}

	private void setApiUrl() {
		URL apiUrl;
		try {
			apiUrl = new URL(RdcEnvironmentVariables.getApiEndpoint().orElse(DEFAULT_API_ENDPOINT));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		provider.setApiUrl(apiUrl);
	}
}