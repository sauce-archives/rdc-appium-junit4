package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcEnvironmentVariables;
import com.saucelabs.rdc.helper.RdcListenerProvider;
import com.saucelabs.rdc.helper.reporter.IntermediateReporter;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static com.saucelabs.rdc.RdcEndpoints.DEFAULT_API_ENDPOINT;

public class RdcTestResultWatcher extends TestWatcher {

	private IntermediateReporter reporter;

	private RdcListenerProvider provider;

	public RdcTestResultWatcher() {
		provider = RdcListenerProvider.newInstance();
	}

	@Override
	protected void succeeded(Description description) {
		reporter.processAndReportResult(true);
	}

	@Override
	protected void failed(Throwable e, Description description) {
		reporter.processAndReportResult(false);
	}

	@Override
	protected void skipped(AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false);
	}

	@Override
	protected void skipped(org.junit.internal.AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false);
	}

	@Override
	protected void finished(Description description) {
		reporter.close();
	}

	public void setRemoteWebDriver(RemoteWebDriver driver) {
		setApiUrl();
		provider.setDriver(driver);
		reporter = new IntermediateReporter(provider);
	}

	private void setApiUrl() {
		URL apiURL;
		try {
			apiURL = new URL(RdcEnvironmentVariables.getApiEndpoint().orElse(DEFAULT_API_ENDPOINT));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		provider.setApiURL(apiURL);
	}

	public void setIsLocalTest(boolean isLocalTest) {
		provider.setLocalTest(isLocalTest);
	}
}