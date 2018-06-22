package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcTestParser;
import com.saucelabs.rdc.model.RdcTest;
import org.junit.Test;
import org.junit.runner.Description;

import static org.junit.Assert.assertEquals;

public class RdcTestParserRdcTest {

	@Test
	public void parseTestFromDescription() {
		String descriptionName = "twoPlusTwoOperation Motorola_Moto_E_2nd_gen_real EU";
		Description description = Description.createTestDescription(Description.class, descriptionName);
		RdcTest test = RdcTestParser.from(description);
		assertEquals("Motorola_Moto_E_2nd_gen_real", test.getDeviceId());
		assertEquals("twoPlusTwoOperation", test.getMethodName());
		assertEquals("org.junit.runner.Description", test.getClassName());
		assertEquals("EU", test.getDataCenterId());
	}
}
