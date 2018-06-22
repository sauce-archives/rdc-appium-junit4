package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestReport {

	private final int id;
	private final RdcTest test;

	@JsonCreator
	public TestReport(@JsonProperty("id") int id, @JsonProperty("test") RdcTest test) {
		this.id = id;
		this.test = test;
	}

	public int getId() {
		return id;
	}

	public RdcTest getTest() {
		return test;
	}
}
