package com.groupeseb.kite;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.restassured.RestAssured;
import com.sun.istack.internal.Nullable;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class DefaultScenarioRunnerTest {
	protected static final int SERVICE_PORT = 8089;
	protected static final String SERVICE_URI = "/myService";

	private WireMockServer wireMockServer;
	private WireMock wireMock;

	@BeforeMethod
	@SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
	void start() {
		wireMockServer = new WireMockServer(wireMockConfig().port(SERVICE_PORT));
		wireMockServer.start();
		WireMock.configureFor("localhost", SERVICE_PORT);
		wireMock = new WireMock("localhost", SERVICE_PORT);

		RestAssured.baseURI = "http://localhost";
		RestAssured.basePath = SERVICE_URI;
		RestAssured.port = SERVICE_PORT;
	}

	@AfterMethod
	void stop() {
		wireMockServer.stop();
	}


	private static void stubForUrlAndBody(String url, int returnCode, @Nullable String retrunBody) {
		ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(returnCode);
		if (retrunBody != null) {
			responseDefBuilder.withBody(retrunBody);
		}
		stubFor(post(urlEqualTo(url)).willReturn(responseDefBuilder));
	}

	/**
	 * test js function
	 */
	@Test
	public void testExecute_02() throws Exception {
		stubForUrlAndBody(SERVICE_URI + "/muUrl01", 201, "myString00000123");
		stubForUrlAndBody(SERVICE_URI + "/muUrl02", 201, "OK");

		new DefaultScenarioRunner().execute(new Scenario("testExecute_02.json"));

		verify(postRequestedFor(urlMatching("/myService/muUrl02"))
				.withRequestBody(matching(".*124.*")));
	}

	/**
	 * test js file function
	 */
	@Test
	public void testExecute_03() throws Exception {
		stubForUrlAndBody(SERVICE_URI + "/muUrl01", 201, "myString00000123");
		stubForUrlAndBody(SERVICE_URI + "/muUrl02", 201, "OK");

		new DefaultScenarioRunner().execute(new Scenario("testExecute_02.json"));

		verify(postRequestedFor(urlMatching("/myService/muUrl02"))
				.withRequestBody(matching(".*124.*")));
	}
}