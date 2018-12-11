package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.Request;

import static java.util.Collections.singletonMap;

public class SuiteReporter {
	public static void updateSuiteReport(
		long suiteId, long reportId, int testReportId, boolean passed, String apiToken) {

		new Request()
			.apiToken(apiToken)
			.path("suites/"+ suiteId + "/reports/" + reportId
					+ "/results/" + testReportId + "/finish")
			.put(singletonMap("passed", passed));
	}
}
