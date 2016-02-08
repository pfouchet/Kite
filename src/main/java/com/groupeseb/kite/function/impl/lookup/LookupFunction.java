package com.groupeseb.kite.function.impl.lookup;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.fail;

@Slf4j
@Component
public class LookupFunction extends Function {
	private static final Pattern ADDITINAL_FUNCTION_PATTERN = Pattern.compile("(.+?):(.+?)", Pattern.CASE_INSENSITIVE);

	private final List<AdditionalLookupFunction> additionalLookupFunctions;

	@Override
	public String getName() {
		return "Lookup";
	}

	@Autowired
	LookupFunction(List<AdditionalLookupFunction> additionalLookupFunctions) {
		this.additionalLookupFunctions = additionalLookupFunctions;
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		String parameter = parameters.get(0);
		Matcher matcher = ADDITINAL_FUNCTION_PATTERN.matcher(parameter);
		boolean hasAdditinalFunction = matcher.matches();
		if (hasAdditinalFunction) {
			parameter = matcher.group(1);
		}

		String objectName = parameter.split("\\.")[0];
		String field = parameter.replace(objectName + '.', "");

		if (creationLog.getBody(objectName) == null) {
			fail(String
					.format("No payload found for %s. Are you sure any request name %s was performed ?",
							objectName, objectName));
		}

		try {
			String fieldValue = JsonPath.read(creationLog.getBody(objectName), field).toString();
			if (hasAdditinalFunction) {
				String additionalParameter = matcher.group(2);
				return applyAddtionalFunction(fieldValue, additionalParameter);
			}
			return fieldValue;
		} catch (PathNotFoundException e) {
			throw new IllegalArgumentException(
					String.format(
							"Cannot apply [LookupFunction]: path not found [%s], on object named [%s]",
							field, objectName), e);
		}
	}

	private String applyAddtionalFunction(String input, String additionalParameter) {
		for (AdditionalLookupFunction additionalLookupFunction : additionalLookupFunctions) {
			if (additionalLookupFunction.match(additionalParameter)) {
				return additionalLookupFunction.apply(input, additionalParameter);
			}
		}
		return input;
	}
}
