package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcEnvironmentVariables;
import com.saucelabs.rdc.helper.RdcTestParser;
import com.saucelabs.rdc.model.DataCenterSuite;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * {@code RdcAppiumSuite} is a JUnit runner that runs your tests for an Appium
 * suite.
 * <p>Appium Suite is a Sauce Labs feature for Appium tests. You create an
 * Appium Suite for a specific app on Sauce Labs. In the process of creating
 * you select an app version and a set of devices. The {@code RdcAppiumSuite}
 * runs your tests with the specified app version on all of the selected
 * devices. You receive a combined report for all your tests at Sauce Labs.
 * <p>For a basic setup you add the runner and an {@link RdcAppiumSuiteWatcher}
 * to your test class and specify the suite
 * <pre>  {@literal @}RunWith(RdcAppiumSuite.class)
 * {@link Rdc @Rdc}(suiteId = 42)
 * public class YourTest {
 *    {@literal @Rule}
 *     public final RdcAppiumSuiteWatcher watcher = new RdcAppiumSuiteWatcher();
 *
 *     private AppiumDriver driver;
 *
 *    {@literal @Before}
 *     public void setup() {
 *         DesiredCapabilities capabilities = new DesiredCapabilities();
 *         capabilities.setCapability({@link RdcCapabilities#API_KEY}, {@link RdcAppiumSuiteWatcher#getApiKey() watcher.getApiKey()});
 *         capabilities.setCapability({@link RdcCapabilities#TEST_REPORT_ID}, {@link RdcAppiumSuiteWatcher#getTestReportId() watcher.getTestReportId()});
 *
 *         driver = new AndroidDriver({@link RdcAppiumSuiteWatcher#getAppiumEndpointUrl() watcher.getAppiumEndpointUrl()}, capabilities);
 *         watcher.setRemoteWebDriver(driver);
 *     }
 *
 *    {@literal @Test}
 *     public void yourTest() {
 *         ... //your test code
 *     }
 * }
 * </pre>
 * <p>The runner needs to know your API key so that it can access Sauce Labs'
 * Real Device Cloud. It reads it from an environment variable
 * {@code "API_KEY"}. For testing purpose you can also set an element
 * {@link Rdc#apiKey() apiKey} of the {@code @Rdc} annotation, but you should
 * not put it in your version control.
 *
 * <h2>Additional Settings</h2>
 *
 * The two settings {@code suiteId} and {@code apiKey} are mandatory but there
 * are also some optional settings that you can use.
 *
 * <h3>App Version</h3>
 * <p>By default your tests are executed with the active version of the app (it
 * is usually the last uploaded version). This is no longer true when you set
 * the app version manually.
 * <p>You can manually set the app version that is used by the suite manually
 * at the Sauce Labs website or you can use the {@code RdcAppiumSuite} runner.
 * It updates the app version when you set the element
 * {@link Rdc#appId() appId} of the {@code @Rdc} annotation or if you set an
 * environment variable {@code "APP_ID"}. The environment variable takes
 * precedence over the annotation.
 *
 * <h3>Timeout</h3>
 * <p>The timeout for each test is 60 minutes by default. You can change the
 * timeout by setting the elements {@link Rdc#timeout() timeout} and
 * {@link Rdc#timeoutUnit()} of the {@code @Rdc} annotation or by setting the
 * environment variables {@code "TIMEOUT"} and {@code "TIMEOUT_UNIT"}. The
 * environment variables takes precedence over the annotation.
 *
 * <h3>Disable Remote Testing</h3>
 * <p>Sometimes it is helpful to run a test against a device that is
 * connected to your local machine. You can disable the suite by setting the
 * element {@link Rdc#testLocally() testLocally} to {@code true} or set
 * the environment variable {@code "TEST_LOCALLY"} to {@code true}.</p>
 *
 * @since 1.0.0
 */
public class RdcAppiumSuite extends Suite {
	private static final List<Runner> NO_RUNNERS = emptyList();
	private final List<Runner> perDeviceRunners;

	private String apiKey;
	private long suiteId;
	private OptionalLong appId;
	private boolean isTestingLocally;
	private SauceLabsApi sauceLabsApi;
	private SuiteReport suiteReport;

	public RdcAppiumSuite(Class<?> clazz) throws InitializationError {
		super(clazz, NO_RUNNERS);
		Rdc rdcAnnotation = rdcAnnotationAtClass(clazz);
		isTestingLocally = isTestingLocally(rdcAnnotation);

		if (isTestingLocally) {
			perDeviceRunners = singletonList(
				new PerDeviceRunner(clazz, null, null, null));
		} else {
			apiKey = getApiKey(rdcAnnotation);
			suiteId = getSuiteId(rdcAnnotation);
			appId = getAppId(rdcAnnotation);

			sauceLabsApi = new SauceLabsApi(apiKey);

			perDeviceRunners = createRunners(clazz);
		}
		this.setScheduler(new ThreadPoolScheduler(perDeviceRunners.size(), getTimeout(rdcAnnotation), getTimeoutUnit(rdcAnnotation)));
	}

	private Rdc rdcAnnotationAtClass(Class<?> clazz) throws InitializationError {
		Rdc rdc = clazz.getAnnotation(Rdc.class);
		if (rdc == null) {
			throw new InitializationError(
				"The test class " + clazz.getName() + " has no annotation @" + Rdc.class.getName());
		} else {
			return rdc;
		}
	}

	private static Set<RdcTest> getTests(Description description) {
		Set<RdcTest> tests = new HashSet<>();
		for (Description childDescription : description.getChildren()) {
			for (Description testDescription : childDescription.getChildren()) {
				tests.add(RdcTestParser.from(testDescription));
			}
		}
		return tests;
	}

	private boolean isTestingLocally(Rdc rdcAnnotation) {
		return RdcEnvironmentVariables.isTestingLocally()
				.orElse(rdcAnnotation.testLocally());
	}

	private String getApiKey(Rdc rdcAnnotation) {
		return requireNonNull(
			RdcEnvironmentVariables.getApiKey().orElse(rdcAnnotation.apiKey()),
			"ApiKey is missing. Please make sure that you set `API_KEY` in your"
				+ " environment variables or set the `apiKey` in your @Rdc annotation.");
	}

	private long getSuiteId(Rdc rdcAnnotation) {
		return requireNonNull(
			RdcEnvironmentVariables.getSuiteId().orElse(rdcAnnotation.suiteId()),
			"suiteId is missing. Please make sure that you set `SUITE_ID` in your"
				+ " environment variables or set the `suiteId` in your @Rdc annotation.");
	}

	private OptionalLong getAppId(Rdc rdcAnnotation) {
		OptionalLong appIdFromEnv = RdcEnvironmentVariables.getAppId();
		if (appIdFromEnv.isPresent()) {
			return appIdFromEnv;
		} else if (rdcAnnotation.appId() > 0){
			return OptionalLong.of(rdcAnnotation.appId());
		} else {
			return OptionalLong.empty();
		}
	}

	private int getTimeout(Rdc rdcAnnotation) {
		return RdcEnvironmentVariables.getTimeout()
				.orElse(rdcAnnotation.timeout());
	}

	private TimeUnit getTimeoutUnit(Rdc rdcAnnotation) {
		return RdcEnvironmentVariables.getTimeoutUnit()
				.orElse(rdcAnnotation.timeoutUnit());
	}

	@Override
	public void run(RunNotifier notifier) {
		if (isTestingLocally) {
			super.run(notifier);
		} else {
			startAppiumSuite();
			try {
				super.run(notifier);
			} finally {
				finishAppiumSuite();
			}
		}
	}

	private void startAppiumSuite() {
		Set<RdcTest> tests = getTests(getDescription());
		suiteReport = sauceLabsApi.startAppiumSuite(tests, suiteId, appId);
	}

	private void finishAppiumSuite() {
		sauceLabsApi.finishAppiumSuite(suiteId, suiteReport.getId());
	}

	protected List<Runner> getChildren() {
		return perDeviceRunners;
	}

	private List<Runner> createRunners(Class<?> clazz) throws InitializationError {
		List<Runner> runners = new LinkedList<>();
		for (DataCenterSuite dataCenterSuite : getDataCenterSuites()) {
			URL appiumUrl = dataCenterSuite.getDataCenterUrl();
			String dataCenterId = dataCenterSuite.dataCenterId;
			for (String deviceId : dataCenterSuite.getDeviceDescriptorIds()) {
				runners.add(new PerDeviceRunner(clazz, deviceId, dataCenterId, appiumUrl));
			}
		}

		if (runners.isEmpty()) {
			throw new RuntimeException("No devices were specified for this suite");
		}
		return runners;
	}

	private Set<DataCenterSuite> getDataCenterSuites() {
		return sauceLabsApi.findDataCenterSuites(suiteId);
	}

	protected static class ThreadPoolScheduler implements RunnerScheduler {

		private final int timeout;
		private final TimeUnit timeoutUnit;

		private final ExecutorService executor;

		public ThreadPoolScheduler(int numberOfThreads, int timeout, TimeUnit timeoutUnit) {
			if (numberOfThreads < 1) {
				throw new RuntimeException("Cannot make a thread pool with " + numberOfThreads + " threads");
			}
			this.timeout = timeout;
			this.timeoutUnit = timeoutUnit;

			executor = Executors.newFixedThreadPool(numberOfThreads);
		}

		public void schedule(final Runnable childStatement) {
			executor.submit(childStatement);
		}

		public void finished() {
			executor.shutdown();
			try {
				executor.awaitTermination(timeout, timeoutUnit);
			} catch (InterruptedException exc) {
				throw new RuntimeException(exc);
			}
		}
	}

	private class PerDeviceRunner extends BlockJUnit4ClassRunner {

		private final String deviceId;
		private final String dataCenterId;
		private final URL appiumUrl;

		public PerDeviceRunner(Class<?> clazz, String deviceId, String dataCenterId, URL appiumUrl) throws InitializationError {
			super(clazz);
			this.deviceId = deviceId;
			this.dataCenterId = dataCenterId;
			this.appiumUrl = appiumUrl;
		}

		@Override
		protected Description describeChild(FrameworkMethod method) {
			String descriptionName = testName(method) + " " + deviceId + " " + dataCenterId;
			return Description.createTestDescription(getTestClass().getJavaClass(), descriptionName, method.getAnnotations());
		}

		@Override
		protected List<TestRule> getTestRules(Object target) {
			List<TestRule> rules = super.getTestRules(target);
			configureSuiteWatcher(rules);
			return rules;
		}

		private void configureSuiteWatcher(List<TestRule> rules) {
			for (TestRule rule : rules) {
				if (rule instanceof RdcAppiumSuiteWatcher) {
					RdcAppiumSuiteWatcher watcher = (RdcAppiumSuiteWatcher) rule;
					watcher.apiKey = apiKey;
					watcher.appiumUrl = appiumUrl;
					watcher.isLocalTest = isTestingLocally;
					watcher.suiteId = suiteId;
					watcher.suiteReport = suiteReport;
				}
			}
		}

		@Override
		protected String getName() {
			return super.getName() + "[" + deviceId + "]";
		}

	}

}
