package com.saucelabs.rdc;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.*;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class RdcTestResultWatcherTest {

	@Parameters(name = "{0}")
	public static Object[][] data() {
		return new Object[][] {
			{
				"successful test",
				(Runnable) () -> {},
				true
			},
			{
				"failing test",
				(Runnable) () -> fail("expected"),
				false
			},
			{
				"errored test",
				(Runnable) () -> { throw new RuntimeException("expected"); },
				false
			},
			{
				"test that fails an assumption",
				(Runnable) () -> assumeTrue("expected", false),
				false
			},
			{
				"test that throws deprecated AssumptionViolatedException",
				(Runnable) () -> {
					throw new AssumptionViolatedException("expected");
				},
				false
			},
		};
	}

	@Parameter(0)
	public String name; //Parameterized requires a field for each parameter

	@Parameter(1)
	public Runnable test;

	@Parameter(2)
	public boolean expectedToPass;

	@Rule
	public final WireMockRule wireMockRule
		= new WireMockRule(options().dynamicPort());

	@Rule
	public final EnvironmentVariables environmentVariables
		= new EnvironmentVariables();

	@Rule
	public final SystemErrRule systemErr
		= new SystemErrRule().enableLog().mute();

	@Test
	public void isReportedAsPassed() {
		assumeTrue(expectedToPass);

		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();

		runTest();

		assertEquals("{\"passed\":true}", bodyOfRequest());
	}

	@Test
	public void isNotReportedAsPassed() {
		assumeFalse(expectedToPass);

		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();

		runTest();

		assertEquals("{\"passed\":false}", bodyOfRequest());
	}

	@Test
	public void isReportedToSessionsTestEndpoint() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		SessionId sessionId = randomSessionId();
		webDriverHasSessionId(sessionId);

		runTest();

		wireMockRule.verify(
			putRequestedFor(
				urlEqualTo("/rest/v2/appium/session/" + sessionId + "/test")));
	}

	@Test
	public void isReportedAsJsonDocument() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();

		runTest();

		wireMockRule.verify(
			putRequestedFor(anyUrl())
				.withHeader("Accept", equalTo("application/json")));
	}

	@Test
	public void doesNotFailBecauseWebDriverIsNotSet() {
		TestClass.setWebDriver = false;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	public void writesErrorWhenServerReturnsWrongStatus() {
		TestClass.setWebDriver = true;
		serverSendsResponse(500);
		webDriverHasArbitrarySessionId();

		runTest();

		assertEquals(
			"Test report status might not be updated on Sauce Labs RDC (TestObject). Status: 500\n",
			systemErr.getLogWithNormalizedLineSeparator());
	}

	private void serverSendsResponse(int status) {
		wireMockRule.stubFor(
			put(anyUrl()).willReturn(aResponse().withStatus(status)));
	}

	private void webDriverHasArbitrarySessionId() {
		when(TestClass.webDriver.getSessionId())
			.thenReturn(randomSessionId());
	}

	private void webDriverHasSessionId(SessionId id) {
		when(TestClass.webDriver.getSessionId())
			.thenReturn(id);
	}

	private SessionId randomSessionId() {
		return new SessionId(randomUUID().toString());
	}


	private Result runTest() {
		environmentVariables.set(
			"API_URL", "http://127.0.0.1:" + wireMockRule.port());
		TestClass.test = test;
		when(TestClass.webDriver.getCapabilities())
			.thenReturn(capabilitiesWithApiKey("dummy-api-key"));
		return JUnitCore.runClasses(TestClass.class);
	}

	private Capabilities capabilitiesWithApiKey(String apiKey) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(API_KEY, apiKey);
		return capabilities;
	}

	private String bodyOfRequest() {
		ServeEvent serveEvent = wireMockRule.getAllServeEvents().get(0);
		return serveEvent.getRequest().getBodyAsString();
	}

	private void assertNoUnexpectedException(Result result) {
		for (Failure failure: result.getFailures()) {
			Throwable exception = failure.getException();
			if (!Objects.equals(exception.getMessage(), "expected")) {
				throw new AssertionError(
					"An unexpected exception was thrown.", exception);
			}
		}
	}

	public static class TestClass {

		@Rule
		public RdcTestResultWatcher watcher = new RdcTestResultWatcher();

		static boolean setWebDriver = false;
		static RemoteWebDriver webDriver = mock(RemoteWebDriver.class);

		static Runnable test; //used to inject the test itself

		@Before
		public void setup() {
			if (setWebDriver) {
				watcher.setRemoteWebDriver(webDriver);
			}
		}

		@Test
		public void test() {
			test.run();
		}
	}
}