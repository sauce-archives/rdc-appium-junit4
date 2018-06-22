package com.saucelabs.rdc.helper;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class RdcListenerProvider {

	private RemoteWebDriver remoteWebDriver;

	private URL apiUrl;

	private boolean isLocalTest = false;

	private RdcListenerProvider() {
	}

	public static RdcListenerProvider newInstance() {
		return new RdcListenerProvider();
	}

	public RemoteWebDriver getRemoteWebDriver() {
		return remoteWebDriver;
	}

	public AppiumDriver getAppiumDriver() {
		return (AppiumDriver) remoteWebDriver;
	}

	public void setDriver(RemoteWebDriver driver) {
		this.remoteWebDriver = driver;
	}

	public URL getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(URL apiUrl) {
		this.apiUrl = apiUrl;
	}

	public boolean isLocalTest() {
		return isLocalTest;
	}

	public void setLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
	}

}
