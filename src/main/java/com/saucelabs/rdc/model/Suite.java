package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Suite {

	private final long id;
	private final String title;
	private final long appVersionId;
	private final String frameworkVersion;
	private final Map<String, Set<String>> deviceIds;

	@JsonCreator
	public Suite(
			@JsonProperty("id") long id,
			@JsonProperty("title") String title,
			@JsonProperty("appVersionId") long appVersionId,
			@JsonProperty("frameworkVersion") String frameworkVersion,
			@JsonProperty("deviceIds") Map<String, Set<String>> deviceIds) {

		this.id = id;
		this.title = title;
		this.appVersionId = appVersionId;
		this.frameworkVersion = frameworkVersion;
		this.deviceIds = deviceIds;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public long getAppVersionId() {
		return appVersionId;
	}

	public String getFrameworkVersion() {
		return frameworkVersion;
	}

	public Map<String, Set<String>> getDeviceIds() {
		return deviceIds;
	}
}
