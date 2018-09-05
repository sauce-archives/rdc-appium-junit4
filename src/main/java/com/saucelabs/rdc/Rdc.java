package com.saucelabs.rdc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Configuration of the {@link RdcAppiumSuite} runner.
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Rdc {
	/**
	 * The API key that is used for accessing Sauce Labs' Real Device Cloud.
	 * Please don't put it under version control.
	 */
	String apiKey() default "";

	/**
	 * The ID of the test suite.
	 */
	long suiteId();

	/**
	 * The version of the application that is used for testing. Normally you
	 * don't have to set the app version. By default your tests are executed
	 * with the active version of the app (it is usually the last uploaded
	 * version).
	 */
	long appId() default -1;

	/**
	 * Timeout for a single test. By default the timeout is in minutes. You
	 * can change the time unit by setting {@link #timeoutUnit()}.
	 */
	int timeout() default 60;

	/**
	 * The time unit for the specified {@link #timeout()}.
	 */
	TimeUnit timeoutUnit() default MINUTES;

	/**
	 * The test is executed against a local device when set to {@code true}.
	 */
	boolean testLocally() default false;
}
