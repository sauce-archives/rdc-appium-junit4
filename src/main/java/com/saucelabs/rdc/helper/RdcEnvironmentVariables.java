package com.saucelabs.rdc.helper;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import static java.lang.Long.parseLong;

public class RdcEnvironmentVariables {
	private static final String API_KEY = "API_KEY";
	private static final String SUITE_ID = "SUITE_ID";
	private static final String API_URL = "API_URL";
	private static final String APP_ID = "APP_ID";
	private static final String TIMEOUT = "TIMEOUT";
	private static final String TIMEOUT_UNIT = "TIMEOUT_UNIT";
	private static final String TEST_LOCALLY = "TEST_LOCALLY";

	public static Optional<String> getApiKey() {
		return Optional.ofNullable(System.getenv(API_KEY));
	}

	public static Optional<Long> getSuiteId() {
		return Optional.ofNullable(System.getenv(SUITE_ID)).map(Long::valueOf);
	}

	public static Optional<String> getApiEndpoint() {
		return Optional.ofNullable(System.getenv(API_URL));
	}

	public static OptionalLong getAppId() {
		String appId = System.getenv(APP_ID);
		if (appId == null) {
			return OptionalLong.empty();
		} else {
			return OptionalLong.of(parseLong(appId));
		}
	}

	public static Optional<Integer> getTimeout() {
		return Optional.ofNullable(System.getenv(TIMEOUT)).map(Integer::valueOf);
	}

	public static Optional<TimeUnit> getTimeoutUnit() {
		return Optional.ofNullable(System.getenv(TIMEOUT_UNIT)).map(TimeUnit::valueOf);
	}

	public static Optional<Boolean> isTestingLocally() {
		return Optional.ofNullable(System.getenv(TEST_LOCALLY)).map(Boolean::valueOf);
	}
}
