package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SuiteReport {

	private final long id;
	private final Set<TestReport> testReports;

	@JsonCreator
	public SuiteReport(@JsonProperty("id") long id, @JsonProperty("testReports") Set<TestReport> testReports) {
		this.id = id;
		this.testReports = testReports;
	}

	public long getId() {
		return id;
	}

	public OptionalInt getTestReportId(RdcTest test) {
		for (TestReport testReport : testReports) {
			if (testReport.getTest().equals(test)) {
				return OptionalInt.of(testReport.getId());
			}
		}

		return OptionalInt.empty();
	}
}
