package com.saucelabs.rdc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.saucelabs.rdc.helper.Request;
import com.saucelabs.rdc.model.DataCenterSuite;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import org.openqa.selenium.remote.SessionId;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.util.OptionalLong;
import java.util.Set;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

class SauceLabsApi {
	private static final GenericType<Set<DataCenterSuite>> SET_OF_DATA_CENTER_SUITES
		= new GenericType<>(new TypeReference<Set<DataCenterSuite>>() {}.getType());
	private final String apiToken;

	SauceLabsApi(
		String apiToken
	) {
		this.apiToken = requireNonNull(apiToken, "The API token is missing.");
	}

	Set<DataCenterSuite> findDataCenterSuites(
		long suiteId
	) {
		return request()
			.path("suites/" + suiteId + "/deviceIds")
			.get(SET_OF_DATA_CENTER_SUITES);
	}

	void finishAppiumSuite(
		long suiteId,
		long reportId
	) {
		request()
			.path("suites/" + suiteId + "/reports/" + reportId + "/finish")
			.put("ignored");
	}

	SuiteReport startAppiumSuite(
		Set<RdcTest> tests,
		long suiteId,
		OptionalLong appId
	) {
		Request request = request()
			.path("suites/" + suiteId + "/reports/start");
		if (appId.isPresent()) {
			request = request.queryParam("appId", appId.getAsLong());
		}
		return request.post(tests, SuiteReport.class);
	}

	void updateTestReportStatus(
		long suiteId,
		long reportId,
		int testReportId,
		boolean passed
	) {
		request()
			.path("suites/"+ suiteId + "/reports/" + reportId + "/results/"
				+ testReportId + "/finish")
			.put(singletonMap("passed", passed));
	}

	void updateTestReportStatus(
		SessionId sessionId,
		boolean passed
	) {
		Response response = sendTestReportStatusUpdate(sessionId, passed);
		if (response.getStatus() != 204) {
			System.err.println("Test report status might not be updated on Sauce Labs RDC (TestObject). Status: " + response.getStatus());
		}
	}

	private Response sendTestReportStatusUpdate(
		SessionId sessionId,
		boolean passed
	) {
		return request()
			.path("session/" + sessionId + "/test")
			.put(singletonMap("passed", passed));
	}

	private Request request() {
		return new Request()
			.apiToken(apiToken);
	}
}
