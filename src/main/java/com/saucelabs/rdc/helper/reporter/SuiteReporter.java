package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RdcListenerProvider;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import com.saucelabs.rdc.model.TestResult;
import com.saucelabs.rdc.resource.AppiumReportResource;

import java.util.OptionalInt;

public class SuiteReporter extends ResultReporter {

	private SuiteReport suiteReport;

	private long suiteId;

	public SuiteReporter() {
	}

	public void setSuiteReport(SuiteReport suiteReport) {
		this.suiteReport = suiteReport;
	}

	public void setSuiteId(long suiteId) {
		this.suiteId = suiteId;
	}

	public void processAndReportResult(boolean passed, RdcTest test) {
		processResult(passed);
		reportResult(passed, test);
	}

	private void reportResult(boolean passed, RdcTest test) {
		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed);
		} else {
			updateSuiteReport(suiteReport, test, passed);
		}
	}

	private void updateSuiteReport(SuiteReport suiteReport, RdcTest test, boolean passed) {
		OptionalInt testReportId = suiteReport.getTestReportId(test);
		if (testReportId.isPresent()) {
			new AppiumReportResource(client)
				.finishAppiumTestReport(suiteId, suiteReport.getId(), testReportId.getAsInt(), new TestResult(passed));
		} else {
			throw new IllegalArgumentException("unknown test " + test);
		}
	}

	public SuiteReport suiteReport() {
		return suiteReport;
	}

	public void setProvider(RdcListenerProvider provider) {
		super.provider = provider;
		super.initClient();
	}
}
