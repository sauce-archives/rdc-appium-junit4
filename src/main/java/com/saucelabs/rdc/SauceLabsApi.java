package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.Request;
import org.openqa.selenium.remote.SessionId;

import javax.ws.rs.core.Response;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

class SauceLabsApi {
	private final String apiToken;

	SauceLabsApi(
		String apiToken
	) {
		this.apiToken = requireNonNull(apiToken, "The API token is missing.");
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
