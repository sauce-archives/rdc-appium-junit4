package com.saucelabs.rdc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static com.saucelabs.rdc.RdcEndpoints.DEFAULT_API_ENDPOINT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Rdc {
	String apiKey();

	long suiteId();

	String apiUrl() default DEFAULT_API_ENDPOINT;

	long appId() default -1;

	int timeout() default 60;

	TimeUnit timeoutUnit() default MINUTES;

	boolean testLocally() default false;
}