package com.groupeseb.kite.function.impl.lookup;

import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.KiteContext;
import com.groupeseb.kite.function.AbstractWithParameters;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.fail;

@Slf4j
@Component
public class LookupFunction extends AbstractWithParameters {
	private static final Pattern ADDITINAL_FUNCTION_PATTERN = Pattern.compile("(.+?):(.+?)", Pattern.CASE_INSENSITIVE);

	private final List<AdditionalLookupFunction> additionalLookupFunctions;


	@Autowired
	LookupFunction(List<AdditionalLookupFunction> additionalLookupFunctions) {
		super("Lookup");
		this.additionalLookupFunctions = additionalLookupFunctions;
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		String input = parameters.get(0);
		Matcher matcher = ADDITINAL_FUNCTION_PATTERN.matcher(input);
		if (matcher.matches()) {
			String fieldValue = getFieldValue(context.getKiteContext(), matcher.group(1));
			return applyAddtionalFunction(fieldValue, matcher.group(2));
		}
		return getFieldValue(context.getKiteContext(), input);
	}

	private String applyAddtionalFunction(String input, String additionalParameter) {
		for (AdditionalLookupFunction additionalLookupFunction : additionalLookupFunctions) {
			if (additionalLookupFunction.match(additionalParameter)) {
				return additionalLookupFunction.apply(input, additionalParameter);
			}
		}
		throw new IllegalArgumentException("Cannot find AdditionalLookupFunction for : " + additionalParameter);
	}

	static String getFieldValue(KiteContext kiteContext, String parameter) {
		String objectName = parameter.split("\\.")[0];
		if ("ProfileWithOnePreference".equals(objectName)) {
			String ppp = "";
		}
		String body = kiteContext.getBody(objectName);
		if (body == null) {
			fail(String
					.format("No payload found for %s. Are you sure any request name %s was performed ?",
							objectName, objectName));
		}
		if (!parameter.contains(".")) {
			return body;
		}

		String field = parameter.replace(objectName + '.', "");

		try {
			Object readField = JsonPath.read(body, field);
			if (readField == null) {
				fail(String.format("Lookup : Could not get field [%s] for object %s", field, objectName));
			}
			return readField.toString();
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
					String.format(
							"Cannot apply [LookupFunction]: path not found <%s>, on object named <%s>",
							field, objectName), e);
		}
	}
}
