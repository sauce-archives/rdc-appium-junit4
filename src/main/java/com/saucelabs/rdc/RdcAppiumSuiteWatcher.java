package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcListenerProvider;
import com.saucelabs.rdc.helper.RdcTestParser;
import com.saucelabs.rdc.helper.reporter.SuiteReporter;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.OptionalInt;

public class RdcAppiumSuiteWatcher extends TestWatcher {

	private String apiKey;
	private RdcTest test;
	private boolean isLocalTest;
	private URL appiumUrl;
	private URL apiUrl;

	private SuiteReporter reporter;
	private RdcListenerProvider provider;

	public RdcAppiumSuiteWatcher() {
		provider = RdcListenerProvider.newInstance();
	}

	@Override
	protected void starting(Description description) {
		test = RdcTestParser.from(description);
	}

	@Override
	protected void succeeded(Description description) {
		reporter.processAndReportResult(true, RdcTestParser.from(description));
	}

	@Override
	protected void failed(Throwable e, Description description) {
		reporter.processAndReportResult(false, RdcTestParser.from(description));
	}

	@Override
	protected void skipped(AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false, RdcTestParser.from(description));
	}

	@Override
	protected void skipped(org.junit.internal.AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false, RdcTestParser.from(description));
	}

	@Override
	protected void finished(Description description) {
		reporter.close();
	}

	public void setRemoteWebDriver(RemoteWebDriver driver) {
		provider.setApiUrl(apiUrl);
		provider.setDriver(driver);
		reporter.setProvider(provider);
	}

	public void configure(String apiKey, long suiteId, SuiteReport suiteReport, boolean isLocalTest, URL appiumUrl, URL apiUrl) {
		reporter = new SuiteReporter(suiteId, suiteReport);
		setApiKey(apiKey);
		setIsLocalTest(isLocalTest);
		setAppiumUrl(appiumUrl);
		setApiUrl(apiUrl);
	}

	public void setAppiumUrl(URL appiumUrl) {
		this.appiumUrl = appiumUrl;
	}

	public void setApiUrl(URL apiUrl) {
		this.apiUrl = apiUrl;
	}

	public void setIsLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
		provider.setLocalTest(isLocalTest);
	}

	public String getTestReportId() {
		OptionalInt id = reporter.suiteReport().getTestReportId(test);
		if (id.isPresent()) {
			return Integer.toString(id.getAsInt());
		} else {
			throw new IllegalStateException("test report not present");
		}
	}

	public String getTestDeviceId() {
		return reporter.suiteReport().getTestDeviceId(test)
				.orElseThrow(() -> new IllegalStateException("test device not present"));
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public URL getAppiumEndpointUrl() {
		if (isLocalTest) {
			try {
				return new URL("http://0.0.0.0:4723/wd/hub");
			} catch (MalformedURLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		} else {
			return appiumUrl;
		}
	}
}
