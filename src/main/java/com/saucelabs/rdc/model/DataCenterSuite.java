package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataCenterSuite {
	public Set<String> deviceDescriptorIds;
	public URL dataCenterUrl;
	public String dataCenterId;

	@JsonCreator
	public DataCenterSuite(@JsonProperty("dataCenterId") String dataCenterId, @JsonProperty("dataCenterURL") URL dataCenterUrl,
			@JsonProperty("deviceIds") Set<String> deviceDescriptorIds) {
		this.dataCenterId = dataCenterId;
		this.dataCenterUrl = dataCenterUrl;
		this.deviceDescriptorIds = deviceDescriptorIds;
	}

	public Set<String> getDeviceDescriptorIds() {
		return deviceDescriptorIds;
	}

	public URL getDataCenterUrl() {
		return dataCenterUrl;
	}

	public String getDataCenterId() {
		return dataCenterId;
	}

	@Override public String toString() {
		return "DataCenterSuite{" +
				"deviceDescriptorIds=" + deviceDescriptorIds +
				", dataCenterUrl=" + dataCenterUrl +
				", dataCenterId='" + dataCenterId + '\'' +
				'}';
	}
}
