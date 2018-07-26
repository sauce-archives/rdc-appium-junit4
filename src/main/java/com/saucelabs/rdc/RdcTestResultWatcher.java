package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.reporter.ResultReporter;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static com.saucelabs.rdc.RdcEndpoints.DEFAULT_API_ENDPOINT;
import static com.saucelabs.rdc.helper.RdcEnvironmentVariables.getApiEndpoint;

public class RdcTestResultWatcher implements TestRule {

	private RemoteWebDriver webDriver;

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
					safeReportStatus(true);
				} catch (Throwable e) {
					safeReportStatus(false);
					throw e;
				}
			}
		};
	}

	private void safeReportStatus(boolean status) {
		if (webDriver != null) {
			try {
				reportStatus(status);
			} catch (Exception e) {
				System.err.println(
					"Failed to update test report. Caused by "
						+ e.getLocalizedMessage());
			}
		}
	}

	private void reportStatus(boolean status) throws MalformedURLException {
		ResultReporter reporter = new ResultReporter();
		reporter.setRemoteWebDriver(webDriver);
		reporter.createSuiteReportAndTestReport(status, apiUrl());
		reporter.close();
	}

	private URL apiUrl() throws MalformedURLException {
		return new URL(getApiEndpoint().orElse(DEFAULT_API_ENDPOINT));
	}
}
