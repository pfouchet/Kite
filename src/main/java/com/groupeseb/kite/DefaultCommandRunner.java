package com.groupeseb.kite;

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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

@Slf4j
@Component
public class DefaultCommandRunner {
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
	DefaultCommandRunner(DefaultCheckRunner defaultCheckRunner) {
		this.defaultCheckRunner = defaultCheckRunner;
	}

	void execute(Command command, KiteContext kiteContext) throws Exception {

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
				post(command, kiteContext);
				break;
			case GET:
				get(command, kiteContext);
				break;
			case PUT:
				put(command, kiteContext);
				break;
			case DELETE:
				delete(command, kiteContext);
				break;
			case PATCH:
				patch(command, kiteContext);
				break;
			default:
				throw new RuntimeException(String.format("Verbe %s is not supported", command.getVerb().toUpperCase()));
		}

		log.info('[' + command.getName() + "] OK");
	}

	void post(Command command, KiteContext kiteContext) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] POST "
				+ command.getProcessedURI(kiteContext)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(kiteContext));
		}

		Response postResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(kiteContext))
				.body(getProcessedBodyBytes(command, kiteContext))
				.when().post(command.getProcessedURI(kiteContext));

		String response = postResponse.prettyPrint();
		log.info(response);

		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(postResponse.getStatusCode()));
		runChecks(command.getChecks(kiteContext), response);

		if (command.getAutomaticCheck()) {
			String location = postResponse.getHeader("Location");
			log.info("Checking resource: " + location + "...");
			given().header("Accept-Encoding", UTF_8_ENCODING)
					.headers(command.getProcessedHeaders(kiteContext))
					.expect().statusCode(HttpStatus.SC_OK)
					.when().get(location);

			if (command.getName() != null) {
				kiteContext.addLocation(command.getName(), location);
			}
		}
	}

	void patch(Command command, KiteContext kiteContext) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] PATCH "
				+ command.getProcessedURI(kiteContext)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(kiteContext));
		}

		Response patchResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(kiteContext))
				.body(getProcessedBodyBytes(command, kiteContext))
				.when().patch(command.getProcessedURI(kiteContext));

		String response = patchResponse.prettyPrint();
		log.info(response);

		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(patchResponse.getStatusCode()));
		runChecks(command.getChecks(kiteContext), response);

		if (command.getAutomaticCheck()) {
			String location = patchResponse.getHeader("Location");
			log.info("Checking resource: " + location + "...");
			given().header("Accept-Encoding", UTF_8_ENCODING)
					.headers(command.getProcessedHeaders(kiteContext))
					.expect().statusCode(HttpStatus.SC_OK)
					.when().get(location);

			if (command.getName() != null) {
				kiteContext.addLocation(command.getName(), location);
			}
		}
	}

	private static String performGetRequest(Command command, KiteContext kiteContext, @Nullable HttpParams params) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String requestURI = command.getProcessedURI(kiteContext);
		if (!command.getProcessedURI(kiteContext).contains("http://") && !command.getProcessedURI(kiteContext).contains(
				"https://")) {
			requestURI = RestAssured.baseURI + ':' + RestAssured.port + RestAssured.basePath + command.getProcessedURI(
					kiteContext);
		}

		HttpGet httpget = new HttpGet(requestURI);

		if (params != null) {
			httpget.setParams(httpget.getParams());
		}

		httpget.addHeader("Content-Type", "application/json");
		for (Map.Entry<String, String> header : command.getProcessedHeaders(kiteContext).entrySet()) {
			httpget.addHeader(header.getKey(), header.getValue());
		}

		CloseableHttpResponse response = httpClient.execute(httpget);

		assertEquals(command.getDescription()
						+ " | "
						+ command.getExpectedStatus()
						+ " expected but "
						+ response.getStatusLine().getStatusCode()
						+ " received.",
				(int) command.getExpectedStatus(), response.getStatusLine().getStatusCode());

		try {
			String body = EntityUtils.toString(response.getEntity());
			kiteContext.addBody("%", body);
			if (command.getName() != null) {
				kiteContext.addBody(command.getName(), body);
			}

			return body;
		} catch (Exception ignored) {
			return "";
		}
	}

	void get(Command command, KiteContext kiteContext) throws ParseException, IOException {
		if (command.getPagination() != null) {
			paginatedGet(command, kiteContext);
		} else {
			String responseBody = performGetRequest(command, kiteContext, null);
			runChecks(command.getChecks(kiteContext), responseBody);
		}
	}

	void paginatedGet(Command command, KiteContext kiteContext) throws ParseException, IOException {
		log.info("GET " + command.getProcessedURI(kiteContext) + " (expecting " + command.getExpectedStatus() + ')');

		Integer currentPage = command.getPagination().getStartPage();
		Integer totalPages = currentPage;

		while (currentPage <= totalPages) {
			BasicHttpParams params = new BasicHttpParams();
			params.setParameter(command.getPagination().getPageParameterName(), command.getPagination().getStartPage());
			params.setParameter(command.getPagination().getSizeParameterName(), command.getPagination().getSize());

			String responseBody = performGetRequest(command, kiteContext, params);
			totalPages = JsonPath.read(responseBody, command.getPagination().getTotalPagesField());

			runChecks(command.getChecks(kiteContext), responseBody);
			currentPage++;
		}
	}

	void put(Command command, KiteContext kiteContext) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] PUT "
				+ command.getProcessedURI(kiteContext)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(kiteContext));
		}

		Response putResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(kiteContext))
				.body(getProcessedBodyBytes(command, kiteContext)).log().everything(true)
				.expect().statusCode(command.getExpectedStatus())
				.when().put(command.getProcessedURI(kiteContext));

		String response = putResponse.prettyPrint();
		log.info(response);

		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		runChecks(command.getChecks(kiteContext), response);
	}

	void delete(Command command, KiteContext kiteContext) throws ParseException {
		log.info("DELETE " + command.getProcessedURI(kiteContext) + " (expecting " + command.getExpectedStatus() + ')');
		Response r = given().contentType(JSON_UTF8).headers(command.getProcessedHeaders(kiteContext))
				.body(getProcessedBodyBytes(command, kiteContext)).log().everything(true)
				.expect().statusCode(command.getExpectedStatus())
				.when().delete(command.getProcessedURI(kiteContext));

		runChecks(command.getChecks(kiteContext), r.prettyPrint());

		log.info("Checking resource: " + command.getProcessedURI(kiteContext) + "...");

		if (command.getAutomaticCheck()) {
			given().contentType(JSON_UTF8)
					.expect().statusCode(HttpStatus.SC_NOT_FOUND)
					.when().get(command.getProcessedURI(kiteContext));
		}
	}

	void runChecks(Collection<Check> checks, String responseBody) throws ParseException {
		for (Check check : checks) {
			try {
				defaultCheckRunner.verify(check, responseBody);
			} catch (RuntimeException e) {
				fail("Check [" + check.getDescription() + "] failed : " + e.getMessage());
			}
		}
	}

	/**
	 * Processes and encodes the body of the request using UTF-8 {@link Charset}.
	 * <p>
	 * Processing is done using {@link Command#getProcessedBody(KiteContext)} prior encoding to
	 * bytes.
	 *
	 * @param command     the command to get body from, not null
	 * @param kiteContext the creation log related to the command, not null
	 * @return the body of the request, with placeholders processed and encoded in UTF-8
	 */
    private static byte[] getProcessedBodyBytes(Command command,
                                                KiteContext kiteContext) {
        try {
            return command.getProcessedBody(kiteContext).getBytes(
                    Charset.forName(UTF_8_ENCODING));
        } catch (RuntimeException e) {
            fail("Command [" + command.getDescription() + "] failed : "
                    + e.getMessage());
            throw e;
        }
    }
}
