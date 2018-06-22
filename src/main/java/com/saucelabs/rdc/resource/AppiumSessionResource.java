package com.saucelabs.rdc.resource;

import com.saucelabs.rdc.helper.RestClient;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class AppiumSessionResource {

	private final RestClient client;

	public AppiumSessionResource(RestClient client) {
		this.client = client;
	}

	public Response updateTestReportStatus(String sessionId, boolean passed) {
		return client
				.path("session").path(sessionId)
				.path("test")
				.request(APPLICATION_JSON_TYPE)
				.put(Entity.json(Collections.singletonMap("passed", passed)));
	}
}
