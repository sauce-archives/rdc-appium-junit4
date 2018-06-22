package com.saucelabs.rdc.helper;

import com.saucelabs.rdc.Rdc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RdcAnnotation {
	private final Rdc rdc;

	public RdcAnnotation(Class<?> clazz) {
		rdc = Optional.ofNullable(clazz.getAnnotation(Rdc.class))
				.orElseThrow(() -> new IllegalStateException("class " + clazz + " must be annotated with @" + Rdc.class.getName()));
	}

	public Optional<String> getApiKey() {
		return Optional.of(rdc.apiKey());
	}

	public Optional<Long> getSuiteId() {
		return Optional.of(rdc.suiteId());
	}

	public Optional<String> getApiUrl() {
		return Optional.of(rdc.apiUrl());
	}

	public Optional<Long> getAppId() {
		long value = rdc.appId();
		return value > 0 ? Optional.of(value) : Optional.empty();
	}

	public Optional<Boolean> isTestingLocally() {
		return Optional.of(rdc.testLocally());
	}

	public Optional<Integer> getTimeout() {
		return Optional.of(rdc.timeout());
	}

	public Optional<TimeUnit> getTimeoutUnit() {
		return Optional.of(rdc.timeoutUnit());
	}
}
