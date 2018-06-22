package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.resource.AppiumSessionResource;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.ws.rs.core.Response;
import java.net.URL;

import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static java.util.Objects.requireNonNull;

public class ResultReporter {

	private RemoteWebDriver webDriver;

	public void setRemoteWebDriver(RemoteWebDriver webDriver) {
		this.webDriver = webDriver;
	}

	public void createSuiteReportAndTestReport(boolean passed, URL apiUrl) {
		requireNonNull(webDriver, "The WebDriver instance is not set.");
		try (RestClient client = createClient(apiUrl)) {
			AppiumSessionResource appiumSessionResource = new AppiumSessionResource(client);
			Response response = appiumSessionResource.updateTestReportStatus(webDriver.getSessionId().toString(), passed);
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

	void processResult(boolean passed) {
		if (webDriver == null) {
			throw new IllegalStateException("appium driver must be set using setDriver method");
		}

		if (!passed) {
			webDriver.getPageSource();
			webDriver.getScreenshotAs(OutputType.FILE);
		}
	}
}
