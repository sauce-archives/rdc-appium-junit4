package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.Request;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.ws.rs.core.Response;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;

public class ResultReporter {

	private RemoteWebDriver webDriver;

	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public void createSuiteReportAndTestReport(boolean passed, String apiToken) {
		Response response = updateTestReportStatus(passed, apiToken);
		if (response.getStatus() != 204) {
			System.err.println("Test report status might not be updated on Sauce Labs RDC (TestObject). Status: " + response.getStatus());
		}
	}

	private Response updateTestReportStatus(boolean passed, String apiToken) {
		requireNonNull(webDriver, "The WebDriver instance is not set.");
		return new Request()
			.apiToken(apiToken)
			.path("session/" + webDriver.getSessionId() + "/test")
			.put(singletonMap("passed", passed));
	}
}
