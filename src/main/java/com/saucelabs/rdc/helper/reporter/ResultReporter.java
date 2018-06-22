package com.saucelabs.rdc.helper.reporter;

import com.saucelabs.rdc.helper.RdcListenerProvider;
import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.resource.AppiumSessionResource;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.ws.rs.core.Response;

import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static com.saucelabs.rdc.RdcEndpoints.APPIUM_REST_PATH;
import static java.util.Objects.requireNonNull;

public class ResultReporter {

	protected RestClient client;

	protected RdcListenerProvider provider;

	protected ResultReporter() {
	}

	public ResultReporter(RdcListenerProvider provider) {
		this.provider = provider;
		initClient();
	}

	protected void initClient() {
		String apiEndpoint = this.provider.getApiUrl().toString();

		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();

		this.client = RestClient.Builder.createClient()
				.withEndpoint(apiEndpoint)
				.withToken((String) remoteWebDriver.getCapabilities().getCapability(API_KEY))
				.path(APPIUM_REST_PATH)
				.build();
	}

	public void close() {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			return;
		}

		remoteWebDriver.quit();
		client.close();
	}

	public void createSuiteReportAndTestReport(boolean passed) {
		AppiumSessionResource appiumSessionResource = new AppiumSessionResource(client);
		RemoteWebDriver remoteWebDriver = requireNonNull(
				provider.getRemoteWebDriver(),
				"The WebDriver instance is not set.");
		Response response = appiumSessionResource.updateTestReportStatus(remoteWebDriver.getSessionId().toString(), passed);
		if (response.getStatus() != 204) {
			System.out.println("Test report status might not be updated on Sauce Labs RDC (TestObject). Status: " + response.getStatus());
		}
	}

	public void processResult(boolean passed) {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			throw new IllegalStateException("appium driver must be set using setDriver method");
		}

		if (!passed) {
			requestScreenshotAndPageSource();
		}

		if (provider.isLocalTest()) {
			return;
		}
	}

	public void requestScreenshotAndPageSource() {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		remoteWebDriver.getPageSource();
		remoteWebDriver.getScreenshotAs(OutputType.FILE);
	}
}