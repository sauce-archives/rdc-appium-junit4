package com.saucelabs.rdc.resource;

import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import com.saucelabs.rdc.model.TestReport;
import com.saucelabs.rdc.model.TestResult;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class AppiumReportResource {

	private final RestClient client;

	public AppiumReportResource(RestClient client) {
		this.client = client;
	}

	/**
	 * Start a new suite execution including its test executions
	 */
	public SuiteReport startAppiumSuite(long suiteId, Optional<Long> appId, Set<RdcTest> tests) {
		WebTarget target = client
				.path("suites").path(Long.toString(suiteId))
				.path("reports")
				.path("start");

		appId.ifPresent(id -> target.queryParam("appId", id));

		return target
				.request(APPLICATION_JSON_TYPE)
				.post(Entity.json(tests), SuiteReport.class);
	}

	/**
	 * Marks all test executions contained in the specified suite execution as finished
	 */
	public SuiteReport finishAppiumSuite(long suiteId, SuiteReport.Id suiteReportId) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(suiteReportId.value()))
				.path("finish")
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.json("ignored"), SuiteReport.class);
	}

	/**
	 * Sets the status of the specific test execution and marks it as finished
	 */
	public TestReport finishAppiumTestReport(long suiteId, SuiteReport.Id batchReportId, TestReport.Id testReportId,
			TestResult testResult) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(batchReportId.value()))
				.path("results").path(Integer.toString(testReportId.value()))
				.path("finish")
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.json(testResult), TestReport.class);
	}

}
