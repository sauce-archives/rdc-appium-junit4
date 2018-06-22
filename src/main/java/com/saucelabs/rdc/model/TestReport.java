package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestReport {

	private final Id id;
	private final RdcTest test;

	@JsonCreator
	public TestReport(@JsonProperty("id") Id id, @JsonProperty("test") RdcTest test) {
		this.id = id;
		this.test = test;
	}

	public Id getId() {
		return id;
	}

	public RdcTest getTest() {
		return test;
	}

	public static class Id extends com.saucelabs.rdc.model.Id<Integer> {
		public Id(Integer value) {
			super(value);
		}
	}

}
