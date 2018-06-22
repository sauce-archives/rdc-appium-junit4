package com.saucelabs.rdc.helper;

import com.saucelabs.rdc.model.RdcTest;
import org.junit.runner.Description;

public class RdcTestParser {

	public static RdcTest from(Description testDescription) {
		String className = testDescription.getClassName();

		String[] descriptionName = testDescription.getMethodName().split(" ");
		String methodName = descriptionName[0];
		String deviceId = descriptionName[1];
		String dataCenterId = descriptionName[2];

		return new RdcTest(className, methodName, deviceId, dataCenterId);
	}
}
