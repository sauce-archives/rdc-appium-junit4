package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.Request;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;

import static java.util.Collections.singletonMap;

public class SuiteReporter extends ResultReporter {

	private final SuiteReport suiteReport;
	private final long suiteId;

	public SuiteReporter(long suiteId, SuiteReport suiteReport) {
		this.suiteId = suiteId;
		this.suiteReport = suiteReport;
	}

	public void reportResult(String apiToken, boolean passed, RdcTest test) {
		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed, apiToken);
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
