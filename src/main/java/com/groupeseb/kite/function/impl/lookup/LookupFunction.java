package com.groupeseb.kite.function.impl.lookup;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.testng.Assert.fail;

@Slf4j
@Component
public class LookupFunction extends Function {

	private static final String[] EMPTY_ARRAY = {};
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
		String[] allLoockupParams = parameter.split(":");
		parameter = allLoockupParams[0];
		String objectName = parameter.split("\\.")[0];
		String field = parameter.replace(objectName + '.', "");

		if (creationLog.getBody(objectName) == null) {
			fail(String
					.format("No payload found for %s. Are you sure any request name %s was performed ?",
							objectName, objectName));
		}

		try {
			String s = JsonPath.read(creationLog.getBody(objectName), field).toString();
			return applyAddtionalFunction(s, allLoockupParams);
		} catch (PathNotFoundException e) {
			throw new IllegalArgumentException(
					String.format(
							"Cannot apply [LookupFunction]: path not found [%s], on object named [%s]",
							field, objectName), e);
		}
	}

	private String applyAddtionalFunction(String input, String... allLoockupParams) {
		// expected format fieldPath:functionName:param1:....:paramn

		if (allLoockupParams.length < 1) {
			return input;
		}
		String functionName = allLoockupParams[1];

		for (AdditionalLookupFunction additionalLookupFunction : additionalLookupFunctions) {
			if (additionalLookupFunction.match(functionName)) {
				return additionalLookupFunction.apply(input, extractParams(allLoockupParams));
			}
		}
		return input;
	}

	private static String[] extractParams(String... allLoockupParams) {
		if (allLoockupParams.length < 2) {
			return EMPTY_ARRAY;
		}
		return ArrayUtils.subarray(allLoockupParams, 2, allLoockupParams.length);
	}
}
