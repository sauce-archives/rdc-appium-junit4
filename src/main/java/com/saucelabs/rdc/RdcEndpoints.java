package com.saucelabs.rdc;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Appium remote addresses of Sauce Labs' data centers. You can use them
 * for creating your remote {@code AppiumDriver}.
 * <pre>
 *     driver = new AppiumDriver({@link RdcEndpoints#US}, capabilities);
 * </pre>
 *
 * @since 1.0.0
 */
public class RdcEndpoints {

	public static final URL US = getUrl("https://us1.appium.testobject.com/wd/hub");
	public static final URL EU = getUrl("https://eu1.appium.testobject.com/wd/hub");

	private static URL getUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
