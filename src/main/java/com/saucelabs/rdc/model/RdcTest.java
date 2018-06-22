package com.saucelabs.rdc.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RdcTest {

	private final String className;
	private final String methodName;
	private final String deviceId;
	private final String dataCenterId;

	@JsonCreator
	public RdcTest(@JsonProperty("className") String className, @JsonProperty("methodName") String methodName,
			@JsonProperty("deviceId") String deviceId, @JsonProperty("dataCenterId") String dataCenterId) {
		this.className = className;
		this.methodName = methodName;
		this.deviceId = deviceId;
		this.dataCenterId = dataCenterId;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getDataCenterId() {
		return dataCenterId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RdcTest test = (RdcTest) o;
		return Objects.equals(className, test.className) &&
				Objects.equals(methodName, test.methodName) &&
				Objects.equals(deviceId, test.deviceId) &&
				Objects.equals(dataCenterId, test.dataCenterId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(className, methodName, deviceId, dataCenterId);
	}

	@Override
	public String toString() {
		return "RdcTest{" +
				"className='" + className + '\'' +
				", methodName='" + methodName + '\'' +
				", deviceId='" + deviceId + '\'' +
				", dataCenterId='" + dataCenterId + '\'' +
				'}';
	}
}
