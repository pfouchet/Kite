package com.groupeseb.kite.check;

import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.Json;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;

@Getter
@Slf4j
public class Check {
	private final String description;
	private final String fieldName;
	private final String methodName;
	private final String operatorName;
	private final Object expectedValue;
	private final Json parameters;
	private final Boolean foreach;
	private final Boolean mustMatch;
	private final Boolean skip;
	/**
	 * Enable fail on error : if this option is enabled the test suite stop on first check failed.
	 * If this option is disabled, each check throw a CheckFailException and the test suite failed after log all fail
	 * Check.
	 * Default value = true
	 */
	private final Boolean failonerror;

	@SuppressWarnings("ConstantConditions")
	public Check(Json checkSpecification, ContextProcessor context) throws ParseException {
		checkSpecification.checkExistence("field", "expected");

		if (!checkSpecification.exists("description")) {
			log.warn("'description' field is missing in one of your check.");
		}

		description = checkSpecification.getStringOrDefault("description", "");
		fieldName = context.processPlaceholdersInString(checkSpecification.getString("field"));
		methodName = checkSpecification.getStringOrDefault("method", "nop");
		operatorName = checkSpecification.getStringOrDefault("operator", "equals");
		expectedValue = context.processPlaceholders(checkSpecification.getObject("expected"));
		parameters = checkSpecification.get("parameters");
		foreach = checkSpecification.getBooleanOrDefault("foreach", false);
		mustMatch = checkSpecification.getBooleanOrDefault("mustMatch", foreach);
		skip = checkSpecification.getBooleanOrDefault("skip", false);
		failonerror = checkSpecification.getBooleanOrDefault("failOnError", true);
	}
}
