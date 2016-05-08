package com.groupeseb.kite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.restassured.RestAssured;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nullable;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultScenarioRunnerTest {
	protected static final int SERVICE_PORT = 8089;
	protected static final String SERVICE_URI = "/myService";
	private static final ObjectMapper OBJECT_MAPPER = KiteContext.initObjectMapper();

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


	private static void stubForUrlAndBody(String url, int returnCode, @Nullable Object retrunBody) throws JsonProcessingException {
		ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(returnCode);
		if (retrunBody != null) {
			responseDefBuilder.withBody(OBJECT_MAPPER.writeValueAsString(retrunBody));
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

		KiteRunner.execute("testExecute_02.json");

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

		KiteRunner.execute("testExecute_03.json");

		verify(postRequestedFor(urlMatching("/myService/muUrl02"))
				.withRequestBody(matching(".*124.*")));
	}

	@Test
	public void testJWTFunction_04() throws Exception {
		stubForUrlAndBody(SERVICE_URI + "/urlUsingJwtHeader", 201, "myString00000123");

		KiteRunner.execute("testExecute_04.json");

		verify(postRequestedFor(urlMatching(SERVICE_URI + "/urlUsingJwtHeader"))
				.withHeader("Authorization", matching("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkb21haW5zIjpbeyJrZXkiOiJkb21haW4yIn1dLCJwcm9maWxlVWlkIjoiZmlyc3RVaWQifQ==")));
	}

	/**
	 * update kite context between 2 tests
	 */
	@Test
	public void testKiteContext_05() throws Exception {
		String value1 = "myString00000123";

		stubForUrlAndBody(SERVICE_URI + "/myFirstUrl", 201, new FieldClass(value1));

		KiteContext kiteContext = KiteRunner.execute("testExecute_05_A.json");

		assertThat(kiteContext.getBodyAs("cmdA", FieldClass.class).getField()).isEqualTo(value1);

		String value2 = "myString00000999";
		kiteContext.addBody("cmdA", value2);

		String value3 = "myString00000324";
		kiteContext.addBodyAsJsonString("cmdAA", new FieldClass(value3));

		stubForUrlAndBody(SERVICE_URI + "/mySecondeUrl", 201, value1);
		KiteRunner.execute("testExecute_05_B.json", kiteContext);

		verify(postRequestedFor(urlMatching(SERVICE_URI + "/mySecondeUrl"))
				.withRequestBody(matching(value2 + value3)));
	}

	@AllArgsConstructor
	@Data
	static class FieldClass {
		private final Object field;
	}
}