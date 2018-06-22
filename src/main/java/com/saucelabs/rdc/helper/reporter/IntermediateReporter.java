package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RdcListenerProvider;

public class IntermediateReporter extends ResultReporter {

	public IntermediateReporter(RdcListenerProvider rdcListenerProvider) {
		super(rdcListenerProvider);
	}

	public void processAndReportResult(boolean passed) {
		processResult(passed);
		createSuiteReportAndTestReport(passed);
	}
}
