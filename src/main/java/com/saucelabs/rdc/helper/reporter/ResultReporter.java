package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.Request;
import org.openqa.selenium.remote.SessionId;

import javax.ws.rs.core.Response;

import static java.util.Collections.singletonMap;

public class ResultReporter {
	public static void createSuiteReportAndTestReport(SessionId sessionId, boolean passed, String apiToken) {
		Response response = updateTestReportStatus(sessionId, passed, apiToken);
		if (response.getStatus() != 204) {
			System.err.println("Test report status might not be updated on Sauce Labs RDC (TestObject). Status: " + response.getStatus());
		}
	}

	private static Response updateTestReportStatus(SessionId sessionId, boolean passed, String apiToken) {
		return new Request()
			.apiToken(apiToken)
			.path("session/" + sessionId + "/test")
			.put(singletonMap("passed", passed));
	}
}
