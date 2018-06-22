package com.saucelabs.rdc.helper;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class RdcListenerProvider {

	private RemoteWebDriver remoteWebDriver;

	private URL apiURL;

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

	public void setApiURL(URL apiURL) {
		this.apiURL = apiURL;
	}

	public URL getApiUrl() {
		return apiURL;
	}

	public boolean isLocalTest() {
		return isLocalTest;
	}

	public void setLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
	}

}
