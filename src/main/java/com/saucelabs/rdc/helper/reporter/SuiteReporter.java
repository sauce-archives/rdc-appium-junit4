package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import com.saucelabs.rdc.resource.AppiumReportResource;

import java.net.URL;
import java.util.OptionalInt;

public class SuiteReporter extends ResultReporter {

	private final SuiteReport suiteReport;
	private final long suiteId;

	public SuiteReporter(long suiteId, SuiteReport suiteReport) {
		this.suiteId = suiteId;
		this.suiteReport = suiteReport;
	}

	public void processAndReportResult(boolean passed, RdcTest test, URL apiUrl) {
		processResult(passed);
		reportResult(passed, test, apiUrl);
	}

	private void reportResult(boolean passed, RdcTest test, URL apiUrl) {
		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed, apiUrl);
		} else {
			updateSuiteReport(suiteReport, test, passed, apiUrl);
		}
	}

	private void updateSuiteReport(SuiteReport suiteReport, RdcTest test, boolean passed, URL apiUrl) {
		OptionalInt testReportId = suiteReport.getTestReportId(test);
		if (testReportId.isPresent()) {
			try (RestClient client = createClient(apiUrl)) {
				new AppiumReportResource(client)
					.finishAppiumTestReport(suiteId, suiteReport.getId(), testReportId.getAsInt(), passed);
			}
		} else {
			throw new IllegalArgumentException("unknown test " + test);
		}
	}
}
