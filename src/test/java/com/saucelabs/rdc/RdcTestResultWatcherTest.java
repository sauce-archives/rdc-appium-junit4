package com.saucelabs.rdc;

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
import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.ArrayUtils.subarray;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;

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
	public final FakeSaucelabsServer saucelabsServer
		= new FakeSaucelabsServer();

	@Rule
	public final EnvironmentVariables environmentVariables
		= new EnvironmentVariables();

	@Rule
	public final SystemErrRule systemErr
		= new SystemErrRule().enableLog().mute();

	@Before
	public void resetWebDriverMock() {
		reset(TestClass.webDriver);
	}

	@Test
	public void isReportedAsPassed() {
		assumeTrue(expectedToPass);

		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		SessionId sessionId = randomSessionId();
		webDriverHasSessionId(sessionId);
		wireMockServerIsApiServer();

		runTest();

		assertReported(sessionId, "{\"passed\":true}");
	}

	@Test
	public void isReportedAsPassedEvenWhenRemoteDriverQuitHasBeenCalled() {
		assumeTrue(expectedToPass);

		serverSendsResponse(204);
		SessionId sessionId = randomSessionId();
		webDriverHasSessionId(TestClassThatQuitsWebDriver.webDriver, sessionId);
		wireMockServerIsApiServer();

		runTestThatQuitsWebDriver();

		assertReported(sessionId, "{\"passed\":true}");
	}

	@Test
	public void isReportedAsNotPassed() {
		assumeFalse(expectedToPass);

		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		SessionId sessionId = randomSessionId();
		webDriverHasSessionId(sessionId);
		wireMockServerIsApiServer();

		runTest();

		assertReported(sessionId, "{\"passed\":false}");
	}

	@Test
	public void isReportedAsNotPassedEvenWhenRemoteDriverQuitHasBeenCalled() {
		assumeFalse(expectedToPass);

		serverSendsResponse(204);
		SessionId sessionId = randomSessionId();
		webDriverHasSessionId(TestClassThatQuitsWebDriver.webDriver, sessionId);
		wireMockServerIsApiServer();

		runTestThatQuitsWebDriver();

		assertReported(sessionId, "{\"passed\":false}");
	}

	@Test
	public void doesNotFailBecauseWebDriverIsNotSet() {
		TestClass.setWebDriver = false;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	public void doesNotFailWhenApiUrlIsWrong() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();
		environmentVariables.set("API_URL", "http://127.0.0.1:38346");

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	public void writesErrorWhenApiUrlIsWrong() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();
		environmentVariables.set("API_URL", "http://127.0.0.1:38346");

		runTest();

		String[] errorLog = systemErr.getLogWithNormalizedLineSeparator().split("\n");
		assertArrayEquals(
			new String[] {
				"Failed to update test report. Caused by:",
				"javax.ws.rs.ProcessingException: java.net.ConnectException: Connection refused (Connection refused)",
				"\tat org.glassfish.jersey.client.internal.HttpUrlConnector.apply(HttpUrlConnector.java:284)",
				"\tat org.glassfish.jersey.client.ClientRuntime.invoke(ClientRuntime.java:278)"
			},
			subarray(errorLog, 0, 4));
	}

	@Test
	public void writesErrorWhenServerReturnsWrongStatus() {
		TestClass.setWebDriver = true;
		serverSendsResponse(500);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();

		runTest();

		assertEquals(
			"Test report status might not be updated on Sauce Labs RDC (TestObject). Status: 500\n",
			systemErr.getLogWithNormalizedLineSeparator());
	}

	@Test
	public void quitsWebDriverAtTheEndOfTheTest() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();

		runTest();

		verify(TestClass.webDriver).quit();
	}

	@Test
	public void doesNotFailWhenQuittingWebDriverFails() {
		doThrow(new RuntimeException("dummy reason"))
			.when(TestClass.webDriver).quit();
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();
		environmentVariables.set("API_URL", "http://127.0.0.1:38346");

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	public void writesErrorWhenItFailsToQuitWebDriver() {
		doThrow(new RuntimeException("dummy reason"))
			.when(TestClass.webDriver).quit();
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();

		runTest();

		assertEquals(
			"Failed to quit WebDriver. Caused by dummy reason\n",
			systemErr.getLogWithNormalizedLineSeparator());
	}

	@Test
	public void libraryVersionIsSentWithEachRequest() {
		TestClass.setWebDriver = true;
		serverSendsResponse(204);
		webDriverHasArbitrarySessionId();
		wireMockServerIsApiServer();

		runTest();

		saucelabsServer.assertLibraryVersionIsSentWithEachRequest();
	}

	private void serverSendsResponse(int status) {
		saucelabsServer.stubFor(
			put(anyUrl()).willReturn(aResponse().withStatus(status)));
	}

	private void webDriverHasArbitrarySessionId() {
		when(TestClass.webDriver.getSessionId())
			.thenReturn(randomSessionId());
	}

	private void webDriverHasSessionId(SessionId id) {
		webDriverHasSessionId(TestClass.webDriver, id);
	}

	private void webDriverHasSessionId(RemoteWebDriver webDriver, SessionId id) {
		when(webDriver.getSessionId())
			.thenReturn(id);
	}

	private void wireMockServerIsApiServer() {
		environmentVariables.set(
			"API_URL", "http://127.0.0.1:" + saucelabsServer.port());
	}

	private SessionId randomSessionId() {
		return new SessionId(randomUUID().toString());
	}

	private Result runTest() {
		TestClass.test = test;
		when(TestClass.webDriver.getCapabilities())
			.thenReturn(capabilitiesWithApiKey("dummy-api-key"));
		return JUnitCore.runClasses(TestClass.class);
	}

	private Result runTestThatQuitsWebDriver() {
		TestClassThatQuitsWebDriver.test = test;
		when(TestClassThatQuitsWebDriver.webDriver.getCapabilities())
			.thenReturn(capabilitiesWithApiKey("dummy-api-key"));
		return JUnitCore.runClasses(TestClassThatQuitsWebDriver.class);
	}

	private Capabilities capabilitiesWithApiKey(String apiKey) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(API_KEY, apiKey);
		return capabilities;
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

	private void assertReported(SessionId sessionId, String message) {
		saucelabsServer.verify(
			putRequestedFor(urlEqualTo("/rest/v2/appium/session/" + sessionId + "/test"))
				.withHeader("Accept", equalTo("application/json"))
				.withRequestBody(equalTo(message)));
	}

	public static class TestClass {

		@Rule
		public RdcTestResultWatcher watcher = new RdcTestResultWatcher();

		static boolean setWebDriver = false;
		static final RemoteWebDriver webDriver = mock(RemoteWebDriver.class);

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

	public static class TestClassThatQuitsWebDriver {

		@Rule
		public RdcTestResultWatcher watcher = new RdcTestResultWatcher();

		static final RemoteWebDriver webDriver = mock(RemoteWebDriver.class);

		static Runnable test; //used to inject the test itself

		@Before
		public void setup() {
			watcher.setRemoteWebDriver(webDriver);
		}

		@Test
		public void test() {
			test.run();
		}

		@After
		public void quitWebDriver() {
			//webDriver has no session id after webDriver.quit() has been called
			when(webDriver.getSessionId())
				.thenReturn(null);
		}
	}
}
