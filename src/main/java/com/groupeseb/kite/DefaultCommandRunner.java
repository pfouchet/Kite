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

	void execute(Command command, ContextProcessor context) throws Exception {

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
				post(command, context);
				break;
			case GET:
				get(command, context);
				break;
			case PUT:
				put(command, context);
				break;
			case DELETE:
				delete(command, context);
				break;
			case PATCH:
				patch(command, context);
				break;
			default:
				throw new RuntimeException(String.format("Verbe %s is not supported", command.getVerb().toUpperCase()));
		}

		log.info('[' + command.getName() + "] OK");
	}

	void post(Command command, ContextProcessor context) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] POST "
				+ command.getProcessedURI(context)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(context));
		}

		Response postResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(context))
				.body(getProcessedBodyBytes(command, context))
				.when().post(command.getProcessedURI(context));

		String response = postResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = context.getKiteContext();
		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(postResponse.getStatusCode()));
		runChecks(command.getChecks(context), response);

		if (command.getAutomaticCheck()) {
			String location = postResponse.getHeader("Location");
			log.info("Checking resource: " + location + "...");
			given().header("Accept-Encoding", UTF_8_ENCODING)
					.headers(command.getProcessedHeaders(context))
					.expect().statusCode(HttpStatus.SC_OK)
					.when().get(location);

			if (command.getName() != null) {
				kiteContext.addLocation(command.getName(), location);
			}
		}
	}

	void patch(Command command, ContextProcessor context) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] PATCH "
				+ command.getProcessedURI(context)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(context));
		}

		Response patchResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(context))
				.body(getProcessedBodyBytes(command, context))
				.when().patch(command.getProcessedURI(context));

		String response = patchResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = context.getKiteContext();
		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		assertEquals("Unexpected response status",
				command.getExpectedStatus(),
				Integer.valueOf(patchResponse.getStatusCode()));
		runChecks(command.getChecks(context), response);

		if (command.getAutomaticCheck()) {
			String location = patchResponse.getHeader("Location");
			log.info("Checking resource: " + location + "...");
			given().header("Accept-Encoding", UTF_8_ENCODING)
					.headers(command.getProcessedHeaders(context))
					.expect().statusCode(HttpStatus.SC_OK)
					.when().get(location);

			if (command.getName() != null) {
				kiteContext.addLocation(command.getName(), location);
			}
		}
	}

	private static String performGetRequest(Command command, ContextProcessor context, @Nullable HttpParams params) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();

		String requestURI = command.getProcessedURI(context);
		if (!command.getProcessedURI(context).contains("http://") && !command.getProcessedURI(context).contains(
				"https://")) {
			requestURI = RestAssured.baseURI + ':' + RestAssured.port + RestAssured.basePath + command.getProcessedURI(
					context);
		}

		HttpGet httpget = new HttpGet(requestURI);

		if (params != null) {
			httpget.setParams(httpget.getParams());
		}

		httpget.addHeader("Content-Type", "application/json");
		for (Map.Entry<String, String> header : command.getProcessedHeaders(context).entrySet()) {
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
			KiteContext kiteContext = context.getKiteContext();
			kiteContext.addBody("%", body);
			if (command.getName() != null) {
				kiteContext.addBody(command.getName(), body);
			}

			return body;
		} catch (Exception ignored) {
			return "";
		}
	}

	void get(Command command, ContextProcessor context) throws ParseException, IOException {
		if (command.getPagination() != null) {
			paginatedGet(command, context);
		} else {
			String responseBody = performGetRequest(command, context, null);
			runChecks(command.getChecks(context), responseBody);
		}
	}

	void paginatedGet(Command command, ContextProcessor context) throws ParseException, IOException {
		log.info("GET " + command.getProcessedURI(context) + " (expecting " + command.getExpectedStatus() + ')');

		Integer currentPage = command.getPagination().getStartPage();
		Integer totalPages = currentPage;

		while (currentPage <= totalPages) {
			BasicHttpParams params = new BasicHttpParams();
			params.setParameter(command.getPagination().getPageParameterName(), command.getPagination().getStartPage());
			params.setParameter(command.getPagination().getSizeParameterName(), command.getPagination().getSize());

			String responseBody = performGetRequest(command, context, params);
			totalPages = JsonPath.read(responseBody, command.getPagination().getTotalPagesField());

			runChecks(command.getChecks(context), responseBody);
			currentPage++;
		}
	}

	void put(Command command, ContextProcessor context) throws ParseException {
		log.info('['
				+ command.getName()
				+ "] PUT "
				+ command.getProcessedURI(context)
				+ " (expecting "
				+ command.getExpectedStatus()
				+ ')');

		if (command.getDebug()) {
			log.info('[' + command.getName() + "] " + command.getProcessedBody(context));
		}

		Response putResponse = given()
				.contentType(JSON_UTF8).headers(command.getProcessedHeaders(context))
				.body(getProcessedBodyBytes(command, context)).log().everything(true)
				.expect().statusCode(command.getExpectedStatus())
				.when().put(command.getProcessedURI(context));

		String response = putResponse.prettyPrint();
		log.info(response);

		KiteContext kiteContext = context.getKiteContext();
		kiteContext.addBody("%", response);
		if (command.getName() != null) {
			kiteContext.addBody(command.getName(), response);
		}

		runChecks(command.getChecks(context), response);
	}

	void delete(Command command, ContextProcessor context) throws ParseException {
		log.info("DELETE " + command.getProcessedURI(context) + " (expecting " + command.getExpectedStatus() + ')');
		Response r = given().contentType(JSON_UTF8).headers(command.getProcessedHeaders(context))
				.body(getProcessedBodyBytes(command, context)).log().everything(true)
				.expect().statusCode(command.getExpectedStatus())
				.when().delete(command.getProcessedURI(context));

		runChecks(command.getChecks(context), r.prettyPrint());

		log.info("Checking resource: " + command.getProcessedURI(context) + "...");

		if (command.getAutomaticCheck()) {
			given().contentType(JSON_UTF8)
					.expect().statusCode(HttpStatus.SC_NOT_FOUND)
					.when().get(command.getProcessedURI(context));
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
	 * Processing is done using {@link Command#getProcessedBody(ContextProcessor)} prior encoding to
	 * bytes.
	 *
	 * @param command     the command to get body from, not null
	 * @param context the creation log related to the command, not null
	 * @return the body of the request, with placeholders processed and encoded in UTF-8
	 */
    private static byte[] getProcessedBodyBytes(Command command,
                                                ContextProcessor context) {
        try {
            return command.getProcessedBody(context).getBytes(
                    Charset.forName(UTF_8_ENCODING));
        } catch (RuntimeException e) {
            fail("Command [" + command.getDescription() + "] failed : "
                    + e.getMessage());
            throw e;
        }
    }
}
