package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RestClient;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URL;

import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class ResultReporter {

	private RemoteWebDriver webDriver;

	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public void createSuiteReportAndTestReport(boolean passed, URL apiUrl) {
		requireNonNull(webDriver, "The WebDriver instance is not set.");
		try (RestClient client = createClient(apiUrl)) {
			Response response = updateTestReportStatus(client, webDriver.getSessionId().toString(), passed);
			if (response.getStatus() != 204) {
				System.err.println("Test report status might not be updated on Sauce Labs RDC (TestObject). Status: " + response.getStatus());
			}
		}
	}

	RestClient createClient(URL apiUrl) {
		return RestClient.Builder.createClient()
			.withEndpoint(apiUrl.toString())
			.withToken((String) webDriver.getCapabilities().getCapability(API_KEY))
			.path("/rest/v2/appium")
			.build();
	}

	private Response updateTestReportStatus(
		RestClient client, String sessionId, boolean passed) {
		return client
			.path("session").path(sessionId)
			.path("test")
			.request(APPLICATION_JSON_TYPE)
			.put(Entity.json(singletonMap("passed", passed)));
	}
}
