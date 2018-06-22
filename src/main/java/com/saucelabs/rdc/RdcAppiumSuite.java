package com.saucelabs.rdc;

import com.saucelabs.rdc.helper.RdcAnnotation;
import com.saucelabs.rdc.helper.RdcEnvironmentVariables;
import com.saucelabs.rdc.helper.RdcTestParser;
import com.saucelabs.rdc.helper.RestClient;
import com.saucelabs.rdc.model.DataCenterSuite;
import com.saucelabs.rdc.model.RdcTest;
import com.saucelabs.rdc.model.SuiteReport;
import com.saucelabs.rdc.resource.AppiumReportResource;
import com.saucelabs.rdc.resource.AppiumSuiteResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.saucelabs.rdc.RdcEndpoints.APPIUM_REST_PATH;
import static java.util.Collections.emptyList;

public class RdcAppiumSuite extends Suite {
	private static final List<Runner> NO_RUNNERS = emptyList();
	private List<Runner> perDeviceRunners = new LinkedList<>();
	private RestClient client;

	private String apiKey;
	private long suiteId;
	private Optional<Long> appId;
	private boolean isTestingLocally;

	private SuiteReport suiteReport;
	private String apiEndpoint;

	public RdcAppiumSuite(Class<?> clazz) throws InitializationError {
		super(clazz, NO_RUNNERS);

		RdcAnnotation rdcAnnotation = new RdcAnnotation(clazz);
		isTestingLocally = isTestingLocally(rdcAnnotation);

		if (isTestingLocally) {
			perDeviceRunners.add(new PerDeviceRunner(clazz, null, null, null, null));
		} else {
			apiKey = getApiKey(rdcAnnotation);
			suiteId = getSuiteId(rdcAnnotation);
			appId = getAppId(rdcAnnotation);
			apiEndpoint = getApiEndpoint(rdcAnnotation);

			client = RestClient.Builder.createClient()
					.withEndpoint(apiEndpoint)
					.path(APPIUM_REST_PATH)
					.withToken(apiKey)
					.build();

			perDeviceRunners = toRunners(clazz, getDataCenterSuites());
		}
		this.setScheduler(new ThreadPoolScheduler(perDeviceRunners.size(), getTimeout(rdcAnnotation), getTimeoutUnit(rdcAnnotation)));
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

	private boolean isTestingLocally(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.isTestingLocally()
				.orElseGet(() -> rdcAnnotation.isTestingLocally().get());

	}

	private String getApiKey(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.getApiKey()
				.orElseGet(() -> rdcAnnotation.getApiKey()
						.orElseThrow(() -> new IllegalArgumentException(
								"ApiKey is missing. Please make sure that you set `API_KEY` in your environment variables" +
										" or set the `apiKey` in your @Rdc annotation.")));

	}

	private long getSuiteId(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.getSuiteId()
				.orElseGet(() -> rdcAnnotation.getSuiteId()
						.orElseThrow(() -> new IllegalArgumentException(
								"suiteId is missing. Please make sure that you set `SUITE_ID` in your environment variables" +
										" or set the `suiteId` in your @Rdc annotation.")));

	}

	private Optional<Long> getAppId(RdcAnnotation rdcAnnotation) {
		Long appId = RdcEnvironmentVariables.getAppId()
				.orElseGet(() -> rdcAnnotation.getAppId()
						.orElse(null));

		return Optional.ofNullable(appId);
	}

	private int getTimeout(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.getTimeout()
				.orElseGet(() -> rdcAnnotation.getTimeout().get());

	}

	private TimeUnit getTimeoutUnit(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.getTimeoutUnit()
				.orElseGet(() -> rdcAnnotation.getTimeoutUnit().get());

	}

	private String getApiEndpoint(RdcAnnotation rdcAnnotation) {
		return RdcEnvironmentVariables.getApiEndpoint()
				.orElseGet(() -> rdcAnnotation.getApiUrl().get());

	}

	@Override
	public void run(RunNotifier notifier) {
		Set<RdcTest> tests = getTests(getDescription());

		if (isTestingLocally) {
			super.run(notifier);
		} else {
			AppiumReportResource appiumReportResource = new AppiumReportResource(client);
			try {
				suiteReport = appiumReportResource.startAppiumSuite(suiteId, appId, tests);
				try {
					super.run(notifier);
				} finally {
					appiumReportResource.finishAppiumSuite(suiteId, suiteReport.getId());
				}
			} finally {
				client.close();
			}
		}
	}

	protected List<Runner> getChildren() {
		return perDeviceRunners;
	}

	private Set<DataCenterSuite> getDataCenterSuites() {
		AppiumSuiteResource suiteReportResource = new AppiumSuiteResource(client);
		return suiteReportResource.readDeviceDescriptorIds(suiteId);
	}

	private List<Runner> toRunners(Class<?> clazz, Collection<DataCenterSuite> dataCenterSuites) throws InitializationError {
		URL apiUrl = null;
		try {
			apiUrl = new URL(apiEndpoint);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		List<Runner> runners = new LinkedList<>();
		for (DataCenterSuite dataCenterSuite : dataCenterSuites) {
			URL appiumURL = dataCenterSuite.getDataCenterURL();
			String dataCenterId = dataCenterSuite.dataCenterId;
			for (String deviceId : dataCenterSuite.getDeviceDescriptorIds()) {
				runners.add(new PerDeviceRunner(clazz, deviceId, dataCenterId, appiumURL, apiUrl));
			}
		}

		if (runners.size() < 1) {
			throw new RuntimeException("No devices were specified for this suite");
		}
		return runners;
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
		private final URL apiUrl;

		public PerDeviceRunner(Class<?> clazz, String deviceId, String dataCenterId, URL appiumUrl, URL apiURL) throws InitializationError {
			super(clazz);
			this.deviceId = deviceId;
			this.dataCenterId = dataCenterId;
			this.appiumUrl = appiumUrl;
			this.apiUrl = apiURL;
		}

		@Override
		protected Description describeChild(FrameworkMethod method) {
			String descriptionName = testName(method) + " " + deviceId + " " + dataCenterId;
			return Description.createTestDescription(getTestClass().getJavaClass(), descriptionName, method.getAnnotations());
		}

		@Override
		protected List<TestRule> getTestRules(Object target) {
			List<TestRule> testRules = super.getTestRules(target);
			for (TestRule testRule : testRules) {
				if (testRule instanceof RdcAppiumSuiteWatcher) {
					RdcAppiumSuiteWatcher resultWatcher = (RdcAppiumSuiteWatcher) testRule;
					resultWatcher.configure(apiKey, suiteId, suiteReport, isTestingLocally, appiumUrl, apiUrl);
				}
			}

			return testRules;
		}

		@Override
		protected String getName() {
			return super.getName() + "[" + deviceId + "]";
		}

	}

}