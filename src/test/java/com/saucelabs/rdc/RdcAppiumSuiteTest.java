package com.saucelabs.rdc;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.saucelabs.rdc.RdcCapabilities.API_KEY;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class RdcAppiumSuiteTest {

	private static final Random RANDOM = new Random();

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
	public final FakeSaucelabsServer saucelabsServer = new FakeSaucelabsServer();

	@Rule
	public final EnvironmentVariables environmentVariables
		= new EnvironmentVariables();

	@Test
	public void runsEveryTestForEveryDevice() {
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
			+ "\"dataCenterURL\":\"http://dummy-url\","
			+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport();
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		Result result = runTest();

		assertEquals(6, result.getRunCount());
	}

	@Test
	public void testPlanIsSentToSauceLabsBeforeRunningTests() {
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport();
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		runTest();

		LoggedRequest secondRequest = saucelabsServer.nthRequest(2);
		assertEquals(
			"[{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"secondTest\",\"deviceId\":\"second-device\",\"dataCenterId\":\"null\"},{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"firstTest\",\"deviceId\":\"first-device\",\"dataCenterId\":\"null\"},{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"firstTest\",\"deviceId\":\"second-device\",\"dataCenterId\":\"null\"},{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"firstTest\",\"deviceId\":\"third-device\",\"dataCenterId\":\"null\"},{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"secondTest\",\"deviceId\":\"third-device\",\"dataCenterId\":\"null\"},{\"className\":\"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\",\"methodName\":\"secondTest\",\"deviceId\":\"first-device\",\"dataCenterId\":\"null\"}]",
			secondRequest.getBodyAsString());
		assertEquals(
			"/rest/v2/appium/suites/123/reports/start",
			secondRequest.getUrl());
	}

	@Test
	@Ignore("bug")
	public void appIdIsUpdatedWhenPresentAsEnvironmentVariable() {
		environmentVariables.set("APP_ID", "798697");
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport();
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		runTest();

		assertEquals(
			"/rest/v2/appium/suites/123/reports/start?appId=798697",
			saucelabsServer.nthRequest(2).getUrl());
	}

	@Test
	public void testsAreReportedAsPassed() {
		assumeTrue(expectedToPass);

		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport(6947697, 45);
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		Result result = runTest();

		assertNoUnexpectedException(result);
		List<LoggedRequest> requests = range(3, 9)
			.mapToObj(saucelabsServer::nthRequest)
			.collect(toList());
		Set<String> urls = requests.stream()
			.map(LoggedRequest::getUrl)
			.collect(toSet());
		assertEquals(new HashSet<>(asList(
			"/rest/v2/appium/suites/123/reports/6947697/results/45/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/46/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/47/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/48/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/49/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/50/finish"
		)), urls);
		List<String> bodies = requests.stream()
			.map(LoggedRequest::getBodyAsString)
			.collect(Collectors.toList());
		requests.stream()
			.map(LoggedRequest::getBodyAsString)
			.forEach(body -> assertEquals("{\"passed\":true}", body));
	}

	@Test
	public void testAreNotReportedAsPassed() {
		assumeFalse(expectedToPass);

		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport(6947697, 45);
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		runTest();

		List<LoggedRequest> requests = range(3, 9)
			.mapToObj(saucelabsServer::nthRequest)
			.collect(toList());
		Set<String> urls = requests.stream()
			.map(LoggedRequest::getUrl)
			.collect(toSet());
		assertEquals(new HashSet<>(asList(
			"/rest/v2/appium/suites/123/reports/6947697/results/45/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/46/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/47/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/48/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/49/finish",
			"/rest/v2/appium/suites/123/reports/6947697/results/50/finish"
		)), urls);
		requests.stream()
			.map(LoggedRequest::getBodyAsString)
			.forEach(body -> assertEquals("{\"passed\":false}", body));
		requests.stream()
			.map(request -> request.getHeader("Accept"))
			.forEach(value -> assertEquals("application/json", value));
	}

	@Test
	@Ignore("Should run for local only. More understanding needed.")
	public void doesNotFailBecauseWebDriverIsNotSet() {
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport();
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = false;

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	@Ignore("Should run for local only. More understanding needed.")
	public void doesNotFailWhenApiUrlIsWrong() {
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport();
		serverAcceptsResult();
		wireMockServerIsApiServer();
		environmentVariables.set("API_URL", "http://127.0.0.1:38346");

		Result result = runTest();

		assertNoUnexpectedException(result);
	}

	@Test
	public void atTheEndSauceLabsReceivesInformationThatAllTestsAreFinished() {
		serverSendsResponse(
			"[{\"dataCenterID\":\"dummy id\","
				+ "\"dataCenterURL\":\"http://dummy-url\","
				+ "\"deviceIds\":[\"first-device\", \"second-device\", \"third-device\"]}]");
		serverSendsSuiteReport(6947697);
		serverAcceptsResult();
		wireMockServerIsApiServer();
		TestClass.setWebDriver = true;

		runTest();

		assertEquals(
			"/rest/v2/appium/suites/123/reports/6947697/finish",
			saucelabsServer.lastRequest().getUrl());
		assertEquals(
			"ignored",
			saucelabsServer.lastRequest().getBodyAsString());
	}

	private void serverSendsResponse(int status) {
		saucelabsServer.stubFor(
			put(anyUrl()).willReturn(aResponse().withStatus(status)));
	}

	private void serverSendsResponse(String messageBody) {
		saucelabsServer.stubFor(
			get(anyUrl()).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(messageBody)));
	}

	private void serverSendsSuiteReport() {
		serverSendsSuiteReport(RANDOM.nextLong());
	}

	private void serverSendsSuiteReport(long suiteId) {
		serverSendsSuiteReport(suiteId, RANDOM.nextInt());
	}

	private void serverSendsSuiteReport(long suiteId, int idOfFirstTest) {
		String messageBody = "{"
			+ "\"id\": " + suiteId + ","
			+ "\"testReports\": ["
			+ "{\"id\": " + idOfFirstTest + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"firstTest\","
			+ "\"deviceId\": \"first-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}},"
			+ "{\"id\": " + (idOfFirstTest + 1) + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"firstTest\","
			+ "\"deviceId\": \"second-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}},"
			+ "{\"id\": " + (idOfFirstTest + 2) + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"firstTest\","
			+ "\"deviceId\": \"third-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}},"
			+ "{\"id\": " + (idOfFirstTest + 3) + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"secondTest\","
			+ "\"deviceId\": \"first-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}},"
			+ "{\"id\": " + (idOfFirstTest + 4) + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"secondTest\","
			+ "\"deviceId\": \"second-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}},"
			+ "{\"id\": " + (idOfFirstTest + 5) + ","
			+ "\"test\": {"
			+ "\"className\": \"com.saucelabs.rdc.RdcAppiumSuiteTest$TestClass\","
			+ "\"methodName\": \"secondTest\","
			+ "\"deviceId\": \"third-device\","
			+ "\"dataCenterId\": \"null\""
			+ "}}"
			+ "]}";
		saucelabsServer.stubFor(
			post(anyUrl()).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(messageBody)));
	}

	private void serverAcceptsResult() {
		saucelabsServer.stubFor(
			put(anyUrl()).willReturn(aResponse().withStatus(200)));
	}

	private void webDriverHasArbitrarySessionId() {
		when(TestClass.webDriver.getSessionId())
			.thenReturn(randomSessionId());
	}

	private void webDriverHasSessionId(SessionId id) {
		when(TestClass.webDriver.getSessionId())
			.thenReturn(id);
	}

	private void wireMockServerIsApiServer() {
		environmentVariables.set(
			"API_URL", "http://127.0.0.1:" + saucelabsServer.port());
	}

	private Result runTest() {
		RdcAppiumSuiteTest.TestClass.test = test;
		when(TestClass.webDriver.getCapabilities())
			.thenReturn(capabilitiesWithApiKey("dummy-api-key"));
		return JUnitCore.runClasses(TestClass.class);
	}

	private Capabilities capabilitiesWithApiKey(String apiKey) {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(API_KEY, apiKey);
		return capabilities;
	}

	private SessionId randomSessionId() {
		return new SessionId(randomUUID().toString());
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

	@RunWith(RdcAppiumSuite.class)
	@Rdc(apiKey = "Your project API key goes here", suiteId = 123)
	public static class TestClass {
		@Rule
		public RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();

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
		public void firstTest() {
			test.run();
		}

		@Test
		public void secondTest() {
			test.run();
		}
	}
}

