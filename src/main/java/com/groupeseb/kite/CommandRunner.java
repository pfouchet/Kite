package com.groupeseb.kite;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.groupeseb.kite.check.Check;
import com.groupeseb.kite.check.DefaultCheckRunner;
import com.jayway.jsonpath.JsonPath;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;

@Slf4j
@Component
public class CommandRunner {
	private static final String UTF_8_ENCODING = "UTF-8";
	private static final String JSON_UTF8 = ContentType.create(
			ContentType.APPLICATION_JSON.getMimeType(), UTF_8_ENCODING).toString();
	private static final String POST = "POST";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	private static final String GET = "GET";
	private static final String PATCH = "PATCH";
	private final DefaultCheckRunner defaultCheckRunner;

	@Autowired
	CommandRunner(DefaultCheckRunner defaultCheckRunner) {
		this.defaultCheckRunner = defaultCheckRunner;
	}

	void execute(Command command, ContextProcessor contextProcessor) throws Exception {

		if (command.getDescription() != null) {
			log.info(command.getDescription() + "...");
		}

		if (command.getDisabled()) {
			log.warn("Disabled command : Skipped.");
			return;
		}

		if (command.getWait() > 0) {
			log.info("Waiting for " + command.getWait() + "ms...");
			Thread.sleep(command.getWait());
		}

		switch (command.getVerb().toUpperCase()) {
			case POST:
				post(command, contextProcessor);
				break;
			case GET:
				get(command, contextProcessor);
				break;
			case PUT:
				put(command, contextProcessor);
				break;
			case DELETE:
				delete(command, contextProcessor);
				break;
			case PATCH:
				patch(command, contextProcessor);
				break;
			default:
				throw new IllegalArgumentException(String.format("Verbe %s is not supported", command.getVerb().toUpperCase()));
		}

		log.info('[' + command.getName() + "] OK");
	}

	void post(Command command, ContextProcessor contextProcessor) throws ParseException {
		String processedURI = contextProcessor.getProcessedURI(command);
		log.info('['
				+ command.getName()
				+ "] POST "
				+ processedURI
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		String processedBody = contextProcessor.getProcessedBody(command);
		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + processedBody);
		}

		Response postResponse = given()
				.contentType(JSON_UTF8).headers(contextProcessor.getProcessedHeaders(command))
				.body(processedBody.getBytes(Charsets.UTF_8))
				.when().post(processedURI);

		String response = postResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = contextProcessor.getKiteContext();
		addBodyIfNotEmpty(kiteContext, response, command);

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(postResponse.getStatusCode()));
		runChecks(contextProcessor.getChecks(command), response);

		if (command.getAutomaticCheck()) {
			doCheck(command, contextProcessor, postResponse, kiteContext);
		}
	}

	private static void doCheck(Command command, ContextProcessor contextProcessor, Response response, KiteContext kiteContext) {
		String location = response.getHeader("Location");
		if (Strings.isNullOrEmpty(location)) {
			throw new IllegalStateException("'Location' is empty in header response, set a valid location or set 'automaticCheck' to false");
		}

		log.info("Checking resource: " + location + "...");
		given().header("Accept-Encoding", UTF_8_ENCODING)
				.headers(contextProcessor.getProcessedHeaders(command))
				.expect().statusCode(HttpStatus.SC_OK)
				.when().get(location);

		if (command.getName() != null) {
			kiteContext.addLocation(command.getName(), location);
		}
	}


	void patch(Command command, ContextProcessor contextProcessor) throws ParseException {
		String processedURI = contextProcessor.getProcessedURI(command);
		log.info('['
				+ command.getName()
				+ "] PATCH "
				+ processedURI
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		String processedBody = contextProcessor.getProcessedBody(command);
		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + processedBody);
		}

		Response patchResponse = given()
				.contentType(JSON_UTF8).headers(contextProcessor.getProcessedHeaders(command))
				.body(processedBody.getBytes(Charsets.UTF_8))
				.when().patch(processedURI);

		String response = patchResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = contextProcessor.getKiteContext();
		addBodyIfNotEmpty(kiteContext, response, command);

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(patchResponse.getStatusCode()));
		runChecks(contextProcessor.getChecks(command), response);

		if (command.getAutomaticCheck()) {
			doCheck(command, contextProcessor, patchResponse, kiteContext);
		}
	}

	private static String performGetRequest(Command command, ContextProcessor contextProcessor, @Nullable HttpParams params) throws IOException {
		String processedURI = contextProcessor.getProcessedURI(command);
		if (!processedURI.contains("http://") && !processedURI.contains("https://")) {
			processedURI = RestAssured.baseURI + ':' + RestAssured.port + RestAssured.basePath + processedURI;
		}

		HttpGet httpget = new HttpGet(processedURI);

		if (params != null) {
			httpget.setParams(httpget.getParams());
		}

		httpget.addHeader("Content-Type", "application/json");
		for (Map.Entry<String, String> header : contextProcessor.getProcessedHeaders(command).entrySet()) {
			httpget.addHeader(header.getKey(), header.getValue());
		}

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
		     CloseableHttpResponse response = httpClient.execute(httpget)) {

			assertEquals(command.getDescription()
							+ " | "
							+ command.getExpectedStatus()
							+ " expected but "
							+ response.getStatusLine().getStatusCode()
							+ " received.",
					(int) command.getExpectedStatus(), response.getStatusLine().getStatusCode());

			String body = EntityUtils.toString(response.getEntity());
			KiteContext kiteContext = contextProcessor.getKiteContext();
			addBodyIfNotEmpty(kiteContext, body, command);
			return body;

		} catch (Exception ignored) {
			return "";
		}
	}

	void get(Command command, ContextProcessor contextProcessor) throws ParseException, IOException {
		if (command.getPagination() != null) {
			paginatedGet(command, contextProcessor);
		} else {
			String responseBody = performGetRequest(command, contextProcessor, null);
			runChecks(contextProcessor.getChecks(command), responseBody);
		}
	}

	void paginatedGet(Command command, ContextProcessor contextProcessor) throws ParseException, IOException {
		log.info("GET " + contextProcessor.getProcessedURI(command) + " (expecting " + command.getExpectedStatus() + ')');

		Integer currentPage = command.getPagination().getStartPage();
		Integer totalPages = currentPage;

		while (currentPage <= totalPages) {
			BasicHttpParams params = new BasicHttpParams();
			params.setParameter(command.getPagination().getPageParameterName(), command.getPagination().getStartPage());
			params.setParameter(command.getPagination().getSizeParameterName(), command.getPagination().getSize());

			String responseBody = performGetRequest(command, contextProcessor, params);
			totalPages = JsonPath.read(responseBody, command.getPagination().getTotalPagesField());

			runChecks(contextProcessor.getChecks(command), responseBody);
			currentPage++;
		}
	}

	void put(Command command, ContextProcessor contextProcessor) throws ParseException {
		String processedURI = contextProcessor.getProcessedURI(command);

		log.info('['
				+ command.getName()
				+ "] PUT "
				+ processedURI
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		String processedBody = contextProcessor.getProcessedBody(command);
		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + processedBody);
		}

		Response putResponse = given()
				.contentType(JSON_UTF8).headers(contextProcessor.getProcessedHeaders(command))
				.body(processedBody.getBytes(Charsets.UTF_8))
				.log().everything(true)
				.expect().statusCode(command.getExpectedStatus())
				.when().put(processedURI);

		String response = putResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = contextProcessor.getKiteContext();
		addBodyIfNotEmpty(kiteContext, response, command);
		runChecks(contextProcessor.getChecks(command), response);
	}

	void delete(Command command, ContextProcessor contextProcessor) throws ParseException {
		String processedURI = contextProcessor.getProcessedURI(command);
		Integer expectedStatus = command.getExpectedStatus();

		log.info("DELETE " + processedURI + " (expecting " + expectedStatus + ')');

		Response r = given()
				.contentType(JSON_UTF8)
				.headers(contextProcessor.getProcessedHeaders(command))
				.body(contextProcessor.getProcessedBody(command).getBytes(Charsets.UTF_8))
				.log()
				.everything(true)
				.expect()
				.statusCode(expectedStatus)
				.when()
				.delete(processedURI);

		runChecks(contextProcessor.getChecks(command), r.prettyPrint());

		log.info("Checking resource: " + processedURI + "...");

		if (command.getAutomaticCheck()) {
			given().contentType(JSON_UTF8)
					.expect().statusCode(HttpStatus.SC_NOT_FOUND)
					.when().get(processedURI);
		}
	}

	void runChecks(Collection<Check> checks, String responseBody) throws ParseException {
		for (Check check : checks) {
			try {
				defaultCheckRunner.verify(check, responseBody);
			} catch (RuntimeException e) {
				throw new IllegalStateException("Check [" + check.getDescription() + "] failed ", e);
			}
		}
	}

	private static void addBodyIfNotEmpty(KiteContext kiteContext, String response, Command command) {
		if (Strings.isNullOrEmpty(response)) {
			return;
		}
		kiteContext.addBody("%", response);
		String name = command.getName();
		if (!Strings.isNullOrEmpty(name)) {
			kiteContext.addBody(name, response);
		}
	}
}
