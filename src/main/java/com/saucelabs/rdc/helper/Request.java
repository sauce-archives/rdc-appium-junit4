package com.saucelabs.rdc.helper;

import com.saucelabs.rdc.RdcAppiumSuite;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static com.saucelabs.rdc.helper.RdcEnvironmentVariables.getApiEndpoint;
import static com.saucelabs.rdc.helper.RestClient.createClientWithApiToken;
import static java.util.Collections.emptyMap;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class Request {
	private final String apiToken;
	private final Map<String, Object> queryParams;
	private final String path;

	public Request() {
		this(null, emptyMap(), null);
	}

	private Request(
		String apiToken,
		Map<String, Object> queryParams, String path
	) {
		this.apiToken = apiToken;
		this.path = path;
		this.queryParams = queryParams;
	}

	public Request apiToken(String apiToken) {
		return new Request(apiToken, queryParams, path);
	}

	public Request path(String path) {
		return new Request(apiToken, queryParams, "/" + path);
	}

	public Request queryParam(String name, Object value) {
		Map<String, Object> newQueryParams = new HashMap<>(queryParams);
		newQueryParams.put(name, value);
		return new Request(apiToken, newQueryParams, path);
	}

	public <T> T get(GenericType<T> responseType) {
		return withClient(
			request -> request.get(responseType)
		);
	}

	public <T> T post(Object entity, Class<T> responseType) {
		return withClient(
			request -> request.post(json(entity), responseType)
		);
	}

	public Response put(Object entity) {
		return withClient(
			request -> request.put(json(entity))
		);
	}

	private <T> T withClient(Function<Invocation.Builder, T> request) {
		String apiEndpoint = getApiEndpoint();
		Client client = createClientWithApiToken(apiToken, apiEndpoint);
		try {
			WebTarget target = client.target(apiEndpoint);
			return request.apply(invocationBuilder(target));
		} finally {
			client.close();
		}
	}

	private Invocation.Builder invocationBuilder(WebTarget target) {
		target = target.path("/rest/v2/appium" + path);
		for (Map.Entry<String, Object> queryParam: queryParams.entrySet()) {
			target = target.queryParam(
				queryParam.getKey(), queryParam.getValue());
		}
		return target
			.request(APPLICATION_JSON_TYPE)
			.header("RDC-Appium-JUnit4-Version", version());
	}

	private String version() {
		try (InputStream stream =
				 RdcAppiumSuite.class.getResourceAsStream("/version.properties")) {
			Properties properties = new Properties();
			properties.load(stream);
			return properties.getProperty("version");
		} catch (IOException e) {
			return "no-version-available";
		}
	}
}
