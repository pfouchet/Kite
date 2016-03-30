package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import com.groupeseb.kite.function.impl.UUIDFunction;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@Data
public class CreationLog {
	private final Map<String, String> uuids = new HashMap<>();
	private final Map<String, String> locations = new HashMap<>();
	private final Map<String, String> variables = new HashMap<>();
	private final Map<String, String> bodies = new HashMap<>();
	private final Map<String, Object> objectVariables = new HashMap<>();

	private Collection<Function> availableFunctions;

	public CreationLog(Collection<Function> availableFunctions) {
		this.availableFunctions = availableFunctions;
	}

	public void extend(CreationLog creationLog) {
		this.uuids.putAll(creationLog.uuids);
		this.locations.putAll(creationLog.locations);
		this.variables.putAll(creationLog.variables);
		this.objectVariables.putAll(creationLog.objectVariables);
	}

	public void addLocation(String name, String location) {
		locations.put(name, location);
	}

	public void addUUIDs(Map<String, String> uuids) {
		this.uuids.putAll(uuids);
	}

	public void addVariable(String key, String value) {
		this.variables.put(key, value);
	}

	public Object getObjectVariable(String objectName) {
		return this.objectVariables.get(objectName);
	}

	public String getVariableValue(String variableName) {
		return this.variables.get(variableName.trim());
	}

	public String getBody(String objectName) {
		return this.bodies.get(objectName);
	}

	private Map<String, String> getEveryUUIDs(String scenario) {
		Pattern uuidPattern = Pattern.compile("\\{\\{" + UUIDFunction.NAME
		                                      + ":(.+?)\\}\\}");
		Matcher uuidMatcher = uuidPattern.matcher(scenario);

		Map<String, String> localUuids = new HashMap<>();

		while (uuidMatcher.find()) {
			String name = uuidMatcher.group(1);

			if (!this.getUuids().containsKey(name)) {
				localUuids.put(name, UUID.randomUUID().toString());
			}
		}

		return localUuids;
	}

	public Function getFunction(String name) {
		for (Function availableFunction : availableFunctions) {
			if (availableFunction.match(name)) {
				return availableFunction;
			}
		}

		return null;
	}

	/**
	 * Applies function with given name on given value with function
	 * placeholders
	 *
	 * @param name                     name of the function to apply
	 * @param valueWithPlaceHolders    value on which function is applied
	 * @param jsonEscapeFunctionResult true if function result must be json-escaped prior replacing
	 *                                 placeholder in valueWithPlaceholder
	 * @return the copy of initial valueWithPlaceholders with function's
	 * placehoders replaced
	 */
	private String executeFunctions(String name, String valueWithPlaceHolders,
	                                boolean jsonEscapeFunctionResult) {
		Pattern withoutParameters = Pattern.compile("\\{\\{" + name + "\\}\\}",
		                                            Pattern.CASE_INSENSITIVE);

		if (withoutParameters.matcher(valueWithPlaceHolders).find()) {
			valueWithPlaceHolders = withoutParameters.matcher(
					valueWithPlaceHolders).replaceAll(
					getFunction(name).apply(new ArrayList<String>(), this));
		} else {
			Pattern pattern = Pattern.compile("\\{\\{" + name
			                                  + "\\:(.+?)\\}\\}", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(valueWithPlaceHolders);

			while (matcher.find()) {
				List<String> parameters = new ArrayList<>();

				for (int i = 1; i <= matcher.groupCount(); ++i) {
					// If function parameter contains JSON special character,
					// they may be encoded by the JSON parser (if value with
					// placeholder is a JSON String).
					// It is necessary to unecape them before using
					// them in the function
					String paramValue = StringEscapeUtils.unescapeJson(matcher
							                                                   .group(i));
					parameters.add(paramValue);
				}

				String functionResult = getFunction(name).apply(parameters,
				                                                this);
				if (jsonEscapeFunctionResult) {
					functionResult = JSONObject.escape(functionResult);
				}

				valueWithPlaceHolders = valueWithPlaceHolders.replace(
						matcher.group(0), functionResult);
			}
		}
		return valueWithPlaceHolders;
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
	String applyFunctions(String valueWithPlaceholders,
	                      boolean jsonEscapeFunctionResult) {
		String processedValue = new String(valueWithPlaceholders);

		for (Function availableFunction : availableFunctions) {
			processedValue = executeFunctions(availableFunction.getName(),
			                                  processedValue, jsonEscapeFunctionResult);
		}

		// 'Timestamp' is not implemented like other functions, because that
		// would not permit to
		// generate the same date for the whole command (since function is
		// called for each
		// placeholder and not one time by)
		String currentDateString = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT
				.format(new Date());
		if (jsonEscapeFunctionResult) {
			currentDateString = JSONObject.escape(currentDateString);
		}
		processedValue = processedValue.replace("{{Timestamp:Now}}",
		                                        currentDateString);
		return processedValue;
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
	public String processPlaceholders(String commandName,
	                                  String valueWithPlaceholders, boolean jsonEscapeFunctionResult) {
		String processedValue = new String(valueWithPlaceholders);

		// Assign UUID for current command if needed
		if (commandName != null) {
			processedValue = processedValue
					.replace("{{" + UUIDFunction.NAME + "}}", "{{"
					                                          + UUIDFunction.NAME + ":" + commandName + "}}");
		}
		// Update UUIDs list to add the one assigned for current command
		this.uuids.putAll(getEveryUUIDs(processedValue));

		processedValue = applyFunctions(processedValue,
		                                jsonEscapeFunctionResult);
		return processedValue;
	}

	public Object processPlaceholders(Object expected) throws ParseException {
		if (expected instanceof String) {
			return processPlaceholdersInString((String) expected);
		} else if (expected instanceof Json) {
			return processPlaceholdersInJSON((Json) expected);
		} else if (expected instanceof Boolean) {
			return expected;
		} else if (expected instanceof Long) {
			return expected;
		} else if (expected instanceof Double) {
			return expected;
		} else {
			throw new NotImplementedException();
		}
	}

	public void addBody(String name, String response) {
		this.bodies.put(name, response);
	}
}
