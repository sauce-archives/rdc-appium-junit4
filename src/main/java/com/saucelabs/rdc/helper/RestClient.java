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
import javax.ws.rs.client.WebTarget;
import java.io.Closeable;
import java.net.URI;
import java.util.Optional;

import static com.saucelabs.rdc.helper.RdcEnvironmentVariables.getApiEndpoint;
import static java.util.Locale.US;

public class RestClient implements Closeable {

	private final Client client;
	private final WebTarget target;

	RestClient(Client client, WebTarget target) {
		this.client = client;
		this.target = target;
	}

	public WebTarget path(String path) {
		return target.path(path);
	}

	@Override
	public void close() {
		client.close();
	}

	public static final class Builder {

		private Client client;
		private String token = "";

		public static Builder createClient() {
			return new Builder();
		}

		private static void addProxyConfiguration(ClientConfig config, String baseUrl) {
			String protocol = URI.create(baseUrl).getScheme().toLowerCase(US);

			Optional<String> proxyHost = Optional.ofNullable(System.getProperty(protocol + ".proxyHost"));
			if (!proxyHost.isPresent()) {
				return;
			}

			String host = proxyHost.get();
			String port = Optional.ofNullable(System.getProperty(protocol + ".proxyPort")).orElse("8080");
			String proxyProtocol = Optional.ofNullable(System.getProperty(protocol + ".proxyProtocol")).orElse("http");
			String url = proxyProtocol + "://" + host + ":" + port;
			config.property(ClientProperties.PROXY_URI, url);

			Optional<String> username = Optional.ofNullable(System.getProperty(protocol + ".proxyUser"));
			Optional<String> password = Optional.ofNullable(System.getProperty(protocol + ".proxyPassword"));
			if (username.isPresent() && password.isPresent()) {
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username.get(), password.get());
				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, credentials);
			}
		}

		public Builder withToken(String token) {
			this.token = token;
			return this;
		}

		public RestClient build() {
			String DISABLE_COOKIES = "jersey.config.apache.client.handleCookies";
			ClientConfig config = new ClientConfig();
			config.property(DISABLE_COOKIES, true);

			String baseUrl = getApiEndpoint();
			addProxyConfiguration(config, baseUrl);

			client = ClientBuilder.newClient(config);

			client.register(new LoggingFeature());
			client.register(HttpAuthenticationFeature.basic(token, ""));

			WebTarget target = client.target(baseUrl + "/rest/v2/appium");
			return new RestClient(client, target);
		}

	}

}
