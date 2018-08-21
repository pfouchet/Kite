package com.groupeseb.kite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * A Command represents a definition of an http call.
 */
@Getter
class Command {
	private static final String VERB_KEY = "verb";
	private static final String URI_KEY = "uri";

	/**
	 * If name is provided with automaticCheck:true,
	 * if returned payload (only for POST/PUT calls) contains a "Location" header, then this location will be saved with the provided name.
	 * Optional.
	 */
	private final String name;

	/**
	 * Description is printed during debug or fail. It is good practice to always provide it.
	 * Optional.
	 */
	private final String description;

	/**
	 * Whether the command should be skipped or not.
	 * Optional.
	 */
	private final Boolean disabled;

	/**
	 * HTTP verb defined as POST, PUT, PATCH, GET, HEAD, DELETE.
	 * Not null.
	 */
	private final String verb;

	/**
	 * Uri of the call.
	 * Value support placeholders such as lookup, variable and location.
	 * Good practice is to provide location.
	 * Not null.
	 */
	private final String uri;

	/**
	 * The body which should sent along the HTTP call. It can contain any placeholders and will be processed beforehand.
	 * Optional.
	 */
	private final String body;

	/**
	 * The expected status. if not provided default value will be set with {@link Command#getExpectedStatusByVerb(java.lang.String)}.
	 * Optional.
	 */
	private final Integer expectedStatus;

	/**
	 * Time to wait before executing HTTP call expressed in milliseconds.
	 * Optional.
	 */
	private final Integer wait;

	/**
	 * Only used during POST and PUT.
	 * Will expect to find a "Location" header in the HTTP response and will execute a subsequent GET on it.
	 * Optional. Default true.
	 */
	private final Boolean automaticCheck;

	/**
	 * Improve verbosity of the query.
	 * Optional.
	 */
	private final Boolean debug;

	/**
	 * Indicate if the HTTP client must URL Encode parameter's values before issuing the request
	 * Used with all verbs
	 * Optional. Default to true
	 */
	private final Boolean urlEncodingEnabled;
	
	/**
	 * Defines headers which will be added to the HTTP call.
	 * Maybe empty.
	 */
	private final Map<String, String> headers;

	/**
	 * Provide multi part.
	 * Optional. Cannot be used with body.
	 */
	@Nullable
	private final MultiPart multiPart;
	
	/**
	 * Provide a repeater mechanism of failure execution.
	 */
	@Nullable
	private final Retry retry;
	
	/**
	 * Only used during GET.
	 * If defined, specific page will be fetched.
	 * Optional.
	 */
	private Pagination pagination = null;

	/**
	 * Internal attribute used to store the raw command representation.
	 */
	private final Json commandSpecification;

	Command(Json commandSpecification) {
		this.commandSpecification = commandSpecification;

		commandSpecification.checkExistence(VERB_KEY, URI_KEY);
		body = commandSpecification.formatFieldToString("body");
		multiPart = initMultiPart(commandSpecification.get("multiPart"));
		if (body != null && multiPart != null) {
			throw new IllegalStateException("One of 'body' or 'multiPart' must be present");
		}
		retry = initRetry(commandSpecification.get("retry"));
		name = commandSpecification.getString("name");
		description = commandSpecification.getString("description");
		verb = requireNonNull(commandSpecification.getString(VERB_KEY));
		uri = commandSpecification.getString(URI_KEY);
		wait = commandSpecification.getIntegerOrDefault("wait", 0);
		disabled = commandSpecification.getBooleanOrDefault("disabled", false);
		expectedStatus = commandSpecification.getIntegerOrDefault("expectedStatus", getExpectedStatusByVerb(verb));
		automaticCheck = commandSpecification.getBooleanOrDefault("automaticCheck",
		                                                          expectedStatus.toString().startsWith("2"));
		debug = commandSpecification.getBooleanOrDefault("debug", false);
		urlEncodingEnabled = commandSpecification.getBooleanOrDefault("urlEncodingEnabled", true);
		
		if (commandSpecification.exists("pagination")) {
			pagination = new Pagination(requireNonNull(commandSpecification.get("pagination")));
		}
		headers = commandSpecification.getMap("headers");
	}

	@Nullable
	private static MultiPart initMultiPart(@Nullable Json multiPartJson) {
		return multiPartJson == null ? null :
		       new MultiPart(
				       requireNonNull(multiPartJson.getString("name"), "multiPart.name is null"),
				       requireNonNull(multiPartJson.getString("fileLocation"), "multiPart.fileLocation is null"));
	}
	
	@Nullable
	private static Retry initRetry(@Nullable Json retryJson) {
		return retryJson == null ? null :
			       new Retry(
				                requireNonNull(retryJson.getLong("timeout"), "retry.timeout is null"),
				                requireNonNull(retryJson.getLong("delay"), "retry.delay is null"));
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
	
	@AllArgsConstructor
	@Getter
	static class MultiPart {
		private final String name;
		private final String fileLocation;
	}
	
	@AllArgsConstructor
	@Getter
	static class Retry {
		private final long timeout;
		private final long delay;
	}
}