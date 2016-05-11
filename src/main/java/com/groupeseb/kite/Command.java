package com.groupeseb.kite;

import lombok.Getter;
import org.apache.http.HttpStatus;

import java.util.Map;

import static java.util.Objects.requireNonNull;

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

	private Pagination pagination = null;

	private final Json commandSpecification;

	Command(Json commandSpecification) {
		this.commandSpecification = commandSpecification;

		commandSpecification.checkExistence(VERB_KEY, URI_KEY);

		name = commandSpecification.getString("name");
		description = commandSpecification.getString("description");
		verb = requireNonNull(commandSpecification.getString(VERB_KEY));
		uri = commandSpecification.getString(URI_KEY);
		wait = commandSpecification.getIntegerOrDefault("wait", 0);
		body = commandSpecification.formatFieldToString("body");
		disabled = commandSpecification.getBooleanOrDefault("disabled", false);
		expectedStatus = commandSpecification.getIntegerOrDefault("expectedStatus", getExpectedStatusByVerb(verb));
		automaticCheck = commandSpecification.getBooleanOrDefault("automaticCheck",
				expectedStatus.toString().startsWith("2"));
		debug = commandSpecification.getBooleanOrDefault("debug", false);

		if (commandSpecification.exists("pagination")) {
			pagination = new Pagination(requireNonNull(commandSpecification.get("pagination")));
		}

		headers = commandSpecification.getMap("headers");
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
}