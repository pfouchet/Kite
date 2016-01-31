package com.groupeseb.kite;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

public class DefaultScenarioRunnerTest {
	protected static final int SERVICE_PORT = 8089;
	protected static final String SERVICE_URI = "/myService";

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(SERVICE_PORT);

	@BeforeClass
	public static void configureRestClient() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.basePath = SERVICE_URI;
		RestAssured.port = SERVICE_PORT;
	}

	/**
	 * test a simple POST
	 */
	@Test
	public void testExecute_01() throws Exception {
		stubFor(post(urlEqualTo(SERVICE_URI + "/resource/resource123"))
				.withHeader("Accept", equalTo("text/xml"))
				.willReturn(aResponse()
						.withStatus(201)
						.withBody("Some content")));

		new DefaultScenarioRunner().execute(new Scenario("testExecute_01.json"));

		verify(postRequestedFor(urlMatching("/myService/resource/[a-z0-9]+"))
				.withRequestBody(matching(".*message-1234-message.*"))
				.withHeader("Content-Type", notMatching("text/xml")));
	}
}