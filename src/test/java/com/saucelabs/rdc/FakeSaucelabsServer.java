package com.saucelabs.rdc;

import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.allRequests;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class FakeSaucelabsServer implements MethodRule {

	private final WireMockRule wireMockRule
		= new WireMockRule(options().dynamicPort());

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return wireMockRule.apply(base, method, target);
	}

	void assertLibraryVersionIsSentWithEachRequest() {
		Set<String> versions = wireMockRule.findAll(allRequests())
			.stream()
			.map(request -> request.getHeader("RDC-Appium-JUnit4-Version"))
			.collect(toSet());
		assertEquals(1, versions.size());
		String version = versions.iterator().next();
		assertTrue(
			"Header RDC-Appium-JUnit4-Version has value " + version
				+ " which is not a valid version",
			version.matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
	}

	void addGetResponse(String path, int status, String messageBody) {
		MappingBuilder request = get(urlPathEqualTo(path))
			.withHeader("application", equalTo("json"));
		ResponseDefinitionBuilder response = aResponse()
			.withStatus(status)
			.withHeader("Content-Type", "application/json")
			.withBody(messageBody);
		stubFor(request.willReturn(response));
	}

	String bodyOfLastRequest() {
		ServeEvent serveEvent = wireMockRule.getAllServeEvents().get(0);
		return serveEvent.getRequest().getBodyAsString();
	}

	LoggedRequest nthRequest(int n) {
		List<ServeEvent> events = wireMockRule.getAllServeEvents();
		ServeEvent event = events.get(events.size() - n);
		return event.getRequest();
	}

	LoggedRequest lastRequest() {
		List<ServeEvent> events = wireMockRule.getAllServeEvents();
		return events.get(0).getRequest();
	}

	LoggedRequest requestWithPath(String path) {
		return wireMockRule.getAllServeEvents().stream()
			.map(ServeEvent::getRequest)
			.filter(request -> request.getUrl().equals(path))
			.findFirst()
			.get();
	}

	public void loadMappingsUsing(MappingsLoader mappingsLoader) {
		wireMockRule.loadMappingsUsing(mappingsLoader);
	}

	public GlobalSettingsHolder getGlobalSettingsHolder() {
		return wireMockRule.getGlobalSettingsHolder();
	}

	public void addMockServiceRequestListener(RequestListener listener) {
		wireMockRule.addMockServiceRequestListener(listener);
	}

	public void enableRecordMappings(FileSource mappingsFileSource, FileSource filesFileSource) {
		wireMockRule.enableRecordMappings(mappingsFileSource, filesFileSource);
	}

	public void stop() {
		wireMockRule.stop();
	}

	public void start() {
		wireMockRule.start();
	}

	public void shutdown() {
		wireMockRule.shutdown();
	}

	public int port() {
		return wireMockRule.port();
	}

	public int httpsPort() {
		return wireMockRule.httpsPort();
	}

	public String url(String path) {
		return wireMockRule.url(path);
	}

	public boolean isRunning() {
		return wireMockRule.isRunning();
	}

	public StubMapping givenThat(MappingBuilder mappingBuilder) {
		return wireMockRule.givenThat(mappingBuilder);
	}

	public StubMapping stubFor(MappingBuilder mappingBuilder) {
		return wireMockRule.stubFor(mappingBuilder);
	}

	public void editStub(MappingBuilder mappingBuilder) {
		wireMockRule.editStub(mappingBuilder);
	}

	public void removeStub(MappingBuilder mappingBuilder) {
		wireMockRule.removeStub(mappingBuilder);
	}

	public void removeStub(StubMapping stubMapping) {
		wireMockRule.removeStub(stubMapping);
	}

	public List<StubMapping> getStubMappings() {
		return wireMockRule.getStubMappings();
	}

	public StubMapping getSingleStubMapping(UUID id) {
		return wireMockRule.getSingleStubMapping(id);
	}

	public List<StubMapping> findStubMappingsByMetadata(StringValuePattern pattern) {
		return wireMockRule.findStubMappingsByMetadata(pattern);
	}

	public void removeStubMappingsByMetadata(StringValuePattern pattern) {
		wireMockRule.removeStubMappingsByMetadata(pattern);
	}

	public void removeStubMapping(StubMapping stubMapping) {
		wireMockRule.removeStubMapping(stubMapping);
	}

	public void verify(RequestPatternBuilder requestPatternBuilder) {
		wireMockRule.verify(requestPatternBuilder);
	}

	public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
		wireMockRule.verify(count, requestPatternBuilder);
	}

	public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
		return wireMockRule.findAll(requestPatternBuilder);
	}

	public List<ServeEvent> getAllServeEvents() {
		return wireMockRule.getAllServeEvents();
	}

	public void setGlobalFixedDelay(int milliseconds) {
		wireMockRule.setGlobalFixedDelay(milliseconds);
	}

	public List<LoggedRequest> findAllUnmatchedRequests() {
		return wireMockRule.findAllUnmatchedRequests();
	}

	public List<NearMiss> findNearMissesForAllUnmatchedRequests() {
		return wireMockRule.findNearMissesForAllUnmatchedRequests();
	}

	public List<NearMiss> findAllNearMissesFor(RequestPatternBuilder requestPatternBuilder) {
		return wireMockRule.findAllNearMissesFor(requestPatternBuilder);
	}

	public List<NearMiss> findNearMissesFor(LoggedRequest loggedRequest) {
		return wireMockRule.findNearMissesFor(loggedRequest);
	}

	public void addStubMapping(StubMapping stubMapping) {
		wireMockRule.addStubMapping(stubMapping);
	}

	public void editStubMapping(StubMapping stubMapping) {
		wireMockRule.editStubMapping(stubMapping);
	}

	public ListStubMappingsResult listAllStubMappings() {
		return wireMockRule.listAllStubMappings();
	}

	public SingleStubMappingResult getStubMapping(UUID id) {
		return wireMockRule.getStubMapping(id);
	}

	public void saveMappings() {
		wireMockRule.saveMappings();
	}

	public void resetAll() {
		wireMockRule.resetAll();
	}

	public void resetRequests() {
		wireMockRule.resetRequests();
	}

	public void resetToDefaultMappings() {
		wireMockRule.resetToDefaultMappings();
	}

	public GetServeEventsResult getServeEvents() {
		return wireMockRule.getServeEvents();
	}

	public SingleServedStubResult getServedStub(UUID id) {
		return wireMockRule.getServedStub(id);
	}

	public void resetScenarios() {
		wireMockRule.resetScenarios();
	}

	public void resetMappings() {
		wireMockRule.resetMappings();
	}

	public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
		return wireMockRule.countRequestsMatching(requestPattern);
	}

	public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
		return wireMockRule.findRequestsMatching(requestPattern);
	}

	public FindRequestsResult findUnmatchedRequests() {
		return wireMockRule.findUnmatchedRequests();
	}

	public void updateGlobalSettings(GlobalSettings newSettings) {
		wireMockRule.updateGlobalSettings(newSettings);
	}

	public FindNearMissesResult findNearMissesForUnmatchedRequests() {
		return wireMockRule.findNearMissesForUnmatchedRequests();
	}

	public GetScenariosResult getAllScenarios() {
		return wireMockRule.getAllScenarios();
	}

	public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
		return wireMockRule.findTopNearMissesFor(loggedRequest);
	}

	public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
		return wireMockRule.findTopNearMissesFor(requestPattern);
	}

	public void startRecording(String targetBaseUrl) {
		wireMockRule.startRecording(targetBaseUrl);
	}

	public void startRecording(RecordSpec spec) {
		wireMockRule.startRecording(spec);
	}

	public void startRecording(RecordSpecBuilder recordSpec) {
		wireMockRule.startRecording(recordSpec);
	}

	public SnapshotRecordResult stopRecording() {
		return wireMockRule.stopRecording();
	}

	public RecordingStatusResult getRecordingStatus() {
		return wireMockRule.getRecordingStatus();
	}

	public SnapshotRecordResult snapshotRecord() {
		return wireMockRule.snapshotRecord();
	}

	public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
		return wireMockRule.snapshotRecord(spec);
	}

	public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
		return wireMockRule.snapshotRecord(spec);
	}

	public Options getOptions() {
		return wireMockRule.getOptions();
	}

	public void shutdownServer() {
		wireMockRule.shutdownServer();
	}

	public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
		return wireMockRule.findAllStubsByMetadata(pattern);
	}

	public void removeStubsByMetadata(StringValuePattern pattern) {
		wireMockRule.removeStubsByMetadata(pattern);
	}
}
