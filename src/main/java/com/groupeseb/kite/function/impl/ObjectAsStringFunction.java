/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{JWT:jwtVariableName}} placeholders by the JWT value corresponding
 * to the object declared in the JWT section of the test.
 * It supports nested placeholders.
 * JWT is not signed and use HS256 algorithm.
 */
@Component
public class ObjectAsStringFunction extends ObjectFunction {

	@Override
	public String getName() {
		return "asString";
	}

	@Override
	protected String innerApply(JSONObject untransformedJsonObject, CreationLog creationLog) {
		String json = untransformedJsonObject.toJSONString();
		return creationLog.processPlaceholdersInString(json);
	}

}
