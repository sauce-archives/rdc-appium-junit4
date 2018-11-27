package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;

import javax.ws.rs.client.Entity;
import java.net.URL;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class SuiteReporter extends ResultReporter {

	private final SuiteReport suiteReport;
	private final long suiteId;

	public SuiteReporter(long suiteId, SuiteReport suiteReport) {
		this.suiteId = suiteId;
		this.suiteReport = suiteReport;
	}

	public void reportResult(boolean passed, RdcTest test) {
		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed);
		} else {
			updateSuiteReport(suiteReport, test, passed);
		}
	}

	private void updateSuiteReport(SuiteReport suiteReport, RdcTest test, boolean passed) {
		int testReportId = suiteReport.getTestReportId(test);
		try (RestClient client = createClient()) {
			client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(suiteReport.getId()))
				.path("results").path(Integer.toString(testReportId))
				.path("finish")
				.request(APPLICATION_JSON_TYPE)
				.header("RDC-Appium-JUnit4-Version", version())
				.put(Entity.json(singletonMap("passed", passed)), Map.class);
		}
	}
}
