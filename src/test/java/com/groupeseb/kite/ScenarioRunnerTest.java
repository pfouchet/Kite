package com.groupeseb.kite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.UrlMatchingStrategy;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.restassured.RestAssured;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.groupeseb.kite.ScenarioRunnerTest.HttpCmdEnum.GET;
import static com.groupeseb.kite.ScenarioRunnerTest.HttpCmdEnum.POST;
import static com.groupeseb.kite.ScenarioRunnerTest.HttpCmdEnum.PUT;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ScenarioRunnerTest {
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

	enum HttpCmdEnum {
		POST, PUT, GET
	}


	private static void stubForUrlAndBody(HttpCmdEnum httpCmd, String url, int returnCode, @Nullable Object retrunBody) throws JsonProcessingException {
		ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(returnCode);
		if (retrunBody != null) {
			responseDefBuilder.withBody(OBJECT_MAPPER.writeValueAsString(retrunBody));
		}
		MappingBuilder cmdBuilder = getMappingBuilder(httpCmd, urlEqualTo(SERVICE_URI + url));
		stubFor(cmdBuilder.willReturn(responseDefBuilder));
	}

	private static MappingBuilder getMappingBuilder(HttpCmdEnum httpCmd, UrlMatchingStrategy urlEqualTo) {
		switch (httpCmd) {
			case POST:
				return post(urlEqualTo);
			case PUT:
				return put(urlEqualTo);
			case GET:
				return get(urlEqualTo);
		}
		throw new IllegalArgumentException("Incorrect HttpCmdEnum : " + httpCmd);
	}

	/**
	 * test js function
	 */
	@Test
	public void testExecute_02() throws Exception {
		stubForUrlAndBody(POST, "/muUrl01", 201, "myString00000123");
		stubForUrlAndBody(POST, "/muUrl02", 201, "OK");

		KiteRunner.getInstance().execute("testExecute_02.json");

		verify(postRequestedFor(urlMatching("/myService/muUrl02"))
				       .withRequestBody(matching(".*124.*")));
	}

	/**
	 * test js file function
	 */
	@Test
	public void testExecute_03() throws Exception {
		stubForUrlAndBody(POST, "/muUrl01", 201, "myString00000123");
		stubForUrlAndBody(POST, "/muUrl02", 201, "OK");

		KiteRunner.getInstance().execute("testExecute_03.json");

		verify(postRequestedFor(urlMatching("/myService/muUrl02"))
				       .withRequestBody(matching(".*124.*")));
	}

	@Test
	public void testJWTFunction_04() throws Exception {
		stubForUrlAndBody(POST, "/urlUsingJwtHeader", 201, "myString00000123");

		KiteRunner.getInstance().execute("testExecute_04.json");

		verify(postRequestedFor(urlMatching(SERVICE_URI + "/urlUsingJwtHeader"))
				       .withHeader("Authorization",
				                   matching(
						                   "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJkb21haW5zIjpbeyJrZXkiOiJkb21haW4yIn1dLCJwcm9maWxlVWlkIjoiZmlyc3RVaWQifQ==")));
	}

	/**
	 * update kite context between 2 tests
	 */
	@Test
	public void testKiteContext_05() throws Exception {
		String value1 = "myString00000123";

		stubForUrlAndBody(POST, "/myFirstUrl", 201, new FieldClass(value1));

		ScenarioRunner scenarioRunner = KiteRunner.getInstance();
		KiteContext kiteContext = scenarioRunner.execute("testExecute_05_A.json");

		assertThat(kiteContext.getBodyAs("cmdA", FieldClass.class).getField()).isEqualTo(value1);

		String value2 = "myString00000999";
		kiteContext.addBody("cmdA", value2);

		String value3 = "myString00000324";
		kiteContext.addBodyAsJsonString("cmdAA", new FieldClass(value3));

		stubForUrlAndBody(POST, "/mySecondeUrl", 201, value1);
		scenarioRunner.execute("testExecute_05_B.json", kiteContext);

		verify(postRequestedFor(urlMatching(SERVICE_URI + "/mySecondeUrl"))
				       .withRequestBody(matching(value2 + value3)));
	}

	/**
	 * put/post cmd with a null body
	 */
	@Test
	public void testKiteContext_06() throws Exception {
		stubForUrlAndBody(PUT, "/nullBodyUrl", 204, null);
		stubForUrlAndBody(POST, "/nullBodyUrl", 201, null);

		KiteRunner.getInstance().execute("testExecute_06.json");
	}

	@Test
	public void testKiteContext_07() throws Exception {
		Map<Object, Object> response = new HashMap<>();
		response.put("anAttribute", "aValue");
		stubForUrlAndBody(POST, "/myUrl07", 201, response);

		KiteRunner.getInstance().execute("testExecute_07.json");
	}

	@Test
	public void testKiteContext_08() throws Exception {
		KiteContext kiteContext = KiteRunner.getInstance().execute("testExecute_08.json");
		Map<String, String> variables = kiteContext.getVariables();
		long currentTimeInt = System.currentTimeMillis() / 1000;

		long timeInt1 = Long.valueOf(requireNonNull(variables.get("timeInt1")));
		assertThat(timeInt1).isBetween(currentTimeInt - 2, currentTimeInt + 2);

		long timeInt2 = Long.valueOf(requireNonNull(variables.get("timeInt2")));
		assertThat(timeInt2).isBetween(currentTimeInt - 102, currentTimeInt - 98);
	}

	/**
	 * test body + multipart case
	 */
	@Test
	public void testKiteContext_09() throws Exception {
		try {
			KiteRunner.getInstance().execute("testExecute_09.json");
			fail("missing error");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage()).isEqualTo("One of 'body' or 'multiPart' must be present");
		}
	}

	/**
	 * test typeOfOperator
	 */
	@Test
	public void testKiteContext_10() throws Exception {
		Map<Object, Object> response = new HashMap<>();
		response.put("number", 1);
		response.put("boolean", true);
		response.put("name", "Heisenberg");
		response.put("created", "2016-11-14T10:47:22");
		response.put("email", "se@seb.com");
		response.put("value", "PRO");
		response.put("regex", "stringwithoutspaceandcaps");
		stubForUrlAndBody(GET, "/myUrl10", 200, response);

		KiteRunner.getInstance().execute("testExecute_10.json");
	}

	@Test
	public void testKiteContext_11_automaticCheckWithAuthorization() throws Exception {
		Map<Object, Object> response = new HashMap<>();
		response.put("anAttribute", "aValue");
		stubForUrlAndBody(POST, "/resources1", 201, response);

		// Stub the POST call : simulate location header
		ResponseDefinitionBuilder responseDefBuilder = aResponse().withStatus(201);
		responseDefBuilder.withHeader("Location", "/resources1/123");
		MappingBuilder cmdBuilder = getMappingBuilder(POST, urlEqualTo(SERVICE_URI + "/resources1"));
		stubFor(cmdBuilder.willReturn(responseDefBuilder));

		// Stub the automatic check call based on location header found during previous POST
		ResponseDefinitionBuilder responseGetDefBuilder = aResponse().withStatus(200);
		MappingBuilder cmdGetBuilder = getMappingBuilder(GET, urlEqualTo(SERVICE_URI + "/resources1/123"));
		stubFor(cmdGetBuilder.willReturn(responseGetDefBuilder));

		KiteContext kiteContext = new KiteContext();
		// Configure the header name used to represent authenticated user.
		kiteContext.setAuthorizationHeaderNameForAutomaticCheck("Authorization");

		// Configure the header value used for the authenticated user.
		kiteContext.addVariable("internalCheckFullyAuthenticated", "123");
		KiteContext resultingKiteContext = KiteRunner.getInstance().execute("testExecute_11.json", kiteContext);


		assertThat(resultingKiteContext.getLocations().get("resource1"))
				.as("Location given by distant server was correctly added to kite context")
				.isNotEmpty();

		verify(getRequestedFor(urlEqualTo(SERVICE_URI + "/resources1/123")).withHeader("Authorization", equalTo("123")));

	}

	@AllArgsConstructor
	@Data
	static class FieldClass {
		private final Object field;
	}
}