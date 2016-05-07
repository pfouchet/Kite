package com.groupeseb.kite;

import com.groupeseb.kite.check.Check;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
class Command {
	private static final String VERB_KEY = "verb";
	private static final String URI_KEY = "uri";

	private final String name;
	private final String description;
	private final Boolean disabled;
	private final String verb;
	private final String uri;
	private final String body;
	private final Integer expectedStatus;
	private final Integer wait;
	private final Boolean automaticCheck;
	private final Boolean debug;
	private final Map<String, String> headers;

	private Pagination pagination;

	private final Json commandSpecification;

	Command(Json commandSpecification) {
		this.commandSpecification = commandSpecification;

		commandSpecification.checkExistence(VERB_KEY, URI_KEY);

		name = commandSpecification.getString("name");
		description = commandSpecification.getString("description");
		verb = commandSpecification.getString(VERB_KEY);
		uri = commandSpecification.getString(URI_KEY);
		wait = commandSpecification.getIntegerOrDefault("wait", 0);
		body = commandSpecification.formatFieldToString("body");
		disabled = commandSpecification.getBooleanOrDefault("disabled", false);
		expectedStatus = commandSpecification.getIntegerOrDefault("expectedStatus", getExpectedStatusByVerb(verb));
		automaticCheck = commandSpecification.getBooleanOrDefault("automaticCheck",
				expectedStatus.toString().startsWith("2"));
		debug = commandSpecification.getBooleanOrDefault("debug", false);

		if (commandSpecification.exists("pagination")) {
			pagination = new Pagination(commandSpecification.get("pagination"));
		}

		headers = commandSpecification.getMap("headers");
	}

	public List<Check> getChecks(KiteContext kiteContext) throws ParseException {
		List<Check> checks = new ArrayList<>();
		for (Integer i = 0; i < commandSpecification.getLength("checks"); ++i) {
			checks.add(new Check(commandSpecification.get("checks").get(i), kiteContext));
		}

		return checks;
	}

	private static int getExpectedStatusByVerb(String string) {
		switch (string) {
			case "POST":
				return HttpStatus.SC_CREATED;
			case "PUT":
				return HttpStatus.SC_NO_CONTENT;
			case "GET":
				return HttpStatus.SC_OK;
			case "DELETE":
				return HttpStatus.SC_NO_CONTENT;
			case "HEAD":
				return HttpStatus.SC_OK;
			case "PATCH":
				return HttpStatus.SC_OK;
			default:
				return HttpStatus.SC_OK;
		}
	}

	String getProcessedURI(KiteContext kiteContext) {
        return kiteContext.processPlaceholders(getName(), getUri(), false);
	}

	String getProcessedBody(KiteContext kiteContext) {
		if (getBody() == null) {
			return "";
		}
        return kiteContext.processPlaceholders(getName(), getBody(),
                true);
	}

	Map<String, String> getProcessedHeaders(KiteContext kiteContext) {
		Map<String, String> processedHeaders = new HashMap<>(getHeaders());

		for (Map.Entry<String, String> entry : processedHeaders.entrySet()) {
			processedHeaders.put(entry.getKey(),
                    kiteContext.processPlaceholders(getName(),
                            processedHeaders.get(entry.getKey()), false));
		}

		if (!processedHeaders.containsKey("Accept")) {
			processedHeaders.put("Accept", "application/json");
		}

		return processedHeaders;
	}
}