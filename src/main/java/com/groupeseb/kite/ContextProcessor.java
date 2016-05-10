package com.groupeseb.kite;

import com.groupeseb.kite.function.AbstractFunction;
import com.groupeseb.kite.function.impl.UUIDFunction;
import lombok.Data;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ContextProcessor {
	private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\{\\{Timestamp:Now\\{\\{", Pattern.CASE_INSENSITIVE);
	private final KiteContext kiteContext;
	private final Collection<AbstractFunction> availableAbstractFunctions;

	ContextProcessor(Collection<AbstractFunction> availableAbstractFunctions,
	                 KiteContext kiteContext) {
		this.availableAbstractFunctions = availableAbstractFunctions;
		this.kiteContext = kiteContext;
	}

	/**
	 * Applies function with given name on given value with function
	 * placeholders
	 *
	 * @param abstractFunction                 current function
	 * @param valueWithPlaceHolders    value on which function is applied
	 * @param jsonEscapeFunctionResult true if function result must be json-escaped prior replacing
	 *                                 placeholder in valueWithPlaceholder
	 * @return the copy of initial valueWithPlaceholders with function's
	 * placehoders replaced
	 */
	private String executeFunctions(AbstractFunction abstractFunction, String valueWithPlaceHolders, boolean jsonEscapeFunctionResult) {

		Matcher matcher = abstractFunction.getMatcher(valueWithPlaceHolders);

		if (abstractFunction.isWithParameters()) {
			String result = valueWithPlaceHolders;
			while (matcher.find()) {
				List<String> parameters = new ArrayList<>();

				for (int i = 1; i <= matcher.groupCount(); ++i) {
					// If function parameter contains JSON special character,
					// they may be encoded by the JSON parser (if value with
					// placeholder is a JSON String).
					// It is necessary to unecape them before using
					// them in the function
					parameters.add(StringEscapeUtils.unescapeJson(matcher.group(i)));
				}

				String functionResult = abstractFunction.apply(parameters, this);
				if (jsonEscapeFunctionResult) {
					functionResult = JSONObject.escape(functionResult);
				}

				result = result.replace(matcher.group(0), functionResult);
			}
			return result;
		}

		return matcher.replaceAll(abstractFunction.apply());
	}

	/**
	 * Apply all available functions for this creation log to replace
	 * placeholders in given value
	 *
	 * @param valueWithPlaceholders    the String containing the placeholders to replace. This String
	 *                                 may be (or not) the representation of a JSON object (with
	 *                                 escaped values in keys and String values). Thus, placeholders
	 *                                 are json-unescaped before being processed (this have no effect
	 *                                 if this parameter is not a Json String)
	 * @param jsonEscapeFunctionResult if true, function result will be json-escaped before being
	 *                                 replaced in processed string. If false, function return is
	 *                                 injected as is.
	 *                                 <p/>
	 *                                 <b>Use true if the result must be directly parsed as JSON</b>,
	 *                                 false otherwise.
	 * @return the copy of initial valueWithPlaceholders with function's
	 * placehoders replaced
	 */
	String applyFunctions(String valueWithPlaceholders, boolean jsonEscapeFunctionResult) {
		String result = valueWithPlaceholders;

		for (AbstractFunction availableAbstractFunction : availableAbstractFunctions) {
			result = executeFunctions(availableAbstractFunction, result, jsonEscapeFunctionResult);
		}

		// 'Timestamp' is not implemented like other functions, because that
		// would not permit to
		// generate the same date for the whole command (since function is
		// called for each
		// placeholder and not one time by)
		String currentDateString = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date());
		if (jsonEscapeFunctionResult) {
			currentDateString = JSONObject.escape(currentDateString);
		}
		return TIMESTAMP_PATTERN.matcher(result).replaceAll(Matcher.quoteReplacement(currentDateString));
	}

	/**
	 * Replace placeholders in given string value by applying functions
	 * available for this creation log
	 *
	 * @param value the String value in which placeholder are replaced
	 * @return a copy of the initial value with function's placehoders replaced
	 */
	public String processPlaceholdersInString(String value) {
		return processPlaceholders(null, value, false);
	}

	/**
	 * Replace placeholders in given json by applying functions available for
	 * this creation log
	 *
	 * @param json The JSON object onto which functions are applied. Functions
	 *             are applied on the String representation of this object
	 *             (obtained with {@link Json#toString()}). In order to keep the
	 *             string valid with respect to JSON syntax, placeholders are
	 *             unescaped before being processed, and their replacing value is
	 *             escaped before being replaced in the String
	 * @return a copy of the given json object with function's placeholders
	 * replaced
	 * @throws ParseException if the string obtained after placeholder replacement is not a
	 *                        valid JSON object
	 */
	private Object processPlaceholdersInJSON(Json json) throws ParseException {
		return new Json(processPlaceholders(null, json.toString(), true));
	}

	private Map<String, String> getEveryUUIDs(String scenario) {
		Pattern uuidPattern = Pattern.compile("\\{\\{" + UUIDFunction.NAME
				+ ":(.+?)\\}\\}");
		Matcher uuidMatcher = uuidPattern.matcher(scenario);

		Map<String, String> localUuids = new HashMap<>();

		while (uuidMatcher.find()) {
			String name = uuidMatcher.group(1);

			if (!this.kiteContext.getUuids().containsKey(name)) {
				localUuids.put(name, UUID.randomUUID().toString());
			}
		}

		return localUuids;
	}

	/**
	 * Replace placeholders in given string by applying functions available for
	 * this creation log
	 * <p/>
	 * Also computes UUID corresponding to given command (if not null) and
	 * update the list of UUIDs for this creation log.
	 *
	 * @param commandName              the name of the command for which placeholders are escaped
	 *                                 (context command)
	 * @param valueWithPlaceholders    the String containing the placeholders to replace. This String
	 *                                 may be (or not) the representation of a JSON object (with
	 *                                 escaped values in keys and String values). Thus, placeholders
	 *                                 are json-unescaped before being processed (this have no effect
	 *                                 if this parameter is not a Json String)
	 * @param jsonEscapeFunctionResult if true, function result will be json-escaped before being
	 *                                 replaced in processed string. If false, function return is
	 *                                 injected as is.
	 *                                 <p/>
	 *                                 <b>Use true if the result must be directly parsed as JSON</b>,
	 *                                 false otherwise.
	 * @return the copy of initial valueWithPlaceholders with function's
	 * placehoders replaced
	 */
	public String processPlaceholders(@Nullable String commandName,
	                                  String valueWithPlaceholders, boolean jsonEscapeFunctionResult) {
		String processedValue = valueWithPlaceholders;

		// Assign UUID for current command if needed
		if (commandName != null) {
			processedValue = processedValue
					.replace("{{" + UUIDFunction.NAME + "}}", "{{"
							+ UUIDFunction.NAME + ':' + commandName + "}}");
		}
		// Update UUIDs list to add the one assigned for current command
		this.kiteContext.getUuids().putAll(getEveryUUIDs(processedValue));

		processedValue = applyFunctions(processedValue,
				jsonEscapeFunctionResult);
		return processedValue;
	}

	public Object processPlaceholders(Object expected) throws ParseException {
		if (expected instanceof String) {
			return processPlaceholdersInString((String) expected);
		}

		if (expected instanceof Json) {
			return processPlaceholdersInJSON((Json) expected);
		}

		if (expected instanceof Boolean ||
				expected instanceof Long ||
				expected instanceof Double) {
			return expected;
		}
		throw new NotImplementedException();
	}
}
