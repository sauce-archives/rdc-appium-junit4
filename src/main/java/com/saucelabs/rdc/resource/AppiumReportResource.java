package com.saucelabs.rdc.resource;

import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import com.saucelabs.rdc.model.TestReport;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.util.OptionalLong;
import java.util.Set;

import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class AppiumReportResource {

	private final RestClient client;

	public AppiumReportResource(RestClient client) {
		this.client = client;
	}

	/**
	 * Start a new suite execution including its test executions
	 */
	public SuiteReport startAppiumSuite(long suiteId, OptionalLong appId, Set<RdcTest> tests) {
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
	public SuiteReport finishAppiumSuite(long suiteId, long suiteReportId) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(suiteReportId))
				.path("finish")
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.json("ignored"), SuiteReport.class);
	}

	/**
	 * Sets the status of the specific test execution and marks it as finished
	 */
	public TestReport finishAppiumTestReport(long suiteId, long batchReportId, int testReportId,
			boolean passed) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(batchReportId))
				.path("results").path(Integer.toString(testReportId))
				.path("finish")
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.json(singletonMap("passed", passed)), TestReport.class);
	}

}
