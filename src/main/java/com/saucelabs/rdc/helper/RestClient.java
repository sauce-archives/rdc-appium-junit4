package com.saucelabs.rdc.helper;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;

import static java.util.Locale.US;

class RestClient {

	private static void addProxyConfiguration(ClientConfig config, String baseUrl) {
		String protocol = URI.create(baseUrl).getScheme().toLowerCase(US);

		String proxyHost = System.getProperty(protocol + ".proxyHost");
		if (proxyHost == null) {
			return;
		}

		String port = propertyOrDefault(protocol + ".proxyPort", "8080");
		String proxyProtocol = propertyOrDefault(protocol + ".proxyProtocol", "http");
		String url = proxyProtocol + "://" + proxyHost + ":" + port;
		config.property(ClientProperties.PROXY_URI, url);

		String username = System.getProperty(protocol + ".proxyUser");
		String password = System.getProperty(protocol + ".proxyPassword");
		if (username != null && password != null) {
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
		}
	}

	static Client createClientWithApiToken(String token, String baseUrl) {
		String DISABLE_COOKIES = "jersey.config.apache.client.handleCookies";
		ClientConfig config = new ClientConfig();
		config.property(DISABLE_COOKIES, true);

		addProxyConfiguration(config, baseUrl);

		Client client = ClientBuilder.newClient(config);

		client.register(new LoggingFeature());
		client.register(HttpAuthenticationFeature.basic(token, ""));

		return client;
	}

	private static String propertyOrDefault(String name, String defaultValue) {
		String value = System.getProperty(name);
		return value == null ? defaultValue : value;
	}
}
