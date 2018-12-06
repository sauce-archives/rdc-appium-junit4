package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.Request;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.saucelabs.rdc.helper.reporter.ResultReporter.createSuiteReportAndTestReport;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public class SuiteReporter {

	private final SuiteReport suiteReport;
	private final long suiteId;
	private RemoteWebDriver webDriver;

	public SuiteReporter(long suiteId, SuiteReport suiteReport) {
		this.suiteId = suiteId;
		this.suiteReport = suiteReport;
	}

	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public void reportResult(String apiToken, boolean passed, RdcTest test) {
		if (suiteReport == null) {
			requireNonNull(webDriver, "The WebDriver instance is not set.");
			createSuiteReportAndTestReport(webDriver.getSessionId(), passed, apiToken);
		} else {
			updateSuiteReport(apiToken, suiteReport, test, passed);
		}
	}

	private void updateSuiteReport(
		String apiToken, SuiteReport suiteReport, RdcTest test, boolean passed) {

		int testReportId = suiteReport.getTestReportId(test);
		new Request()
			.apiToken(apiToken)
			.path("suites/"+ suiteId + "/reports/" + suiteReport.getId()
					+ "/results/" + testReportId + "/finish")
			.put(singletonMap("passed", passed));
	}
}
