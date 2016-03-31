/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{asString:ObjectVariableName}} placeholders by the value corresponding
 * to the object declared in the objectVariables section of the test.
 * It supports nested placeholders, and will transform json into their string representation.
 * Raw String are also supported.
 */
@Component
public class ObjectAsStringFunction extends ObjectFunction {

	@Override
	public String getName() {
		return "asString";
	}

	@Override
	protected String innerApplyOnObject(JSONObject untransformedJsonObject, CreationLog creationLog) {
		String json = untransformedJsonObject.toJSONString();
		return creationLog.processPlaceholdersInString(json);
	}

}
