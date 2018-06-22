package com.saucelabs.rdc;

import java.net.MalformedURLException;
import java.net.URL;

public class RdcEndpoints {

	public static final URL US_ENDPOINT = getUrl("https://us1.appium.testobject.com/wd/hub");
	public static final URL EU_ENDPOINT = getUrl("https://eu1.appium.testobject.com/wd/hub");

	private static URL getUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
