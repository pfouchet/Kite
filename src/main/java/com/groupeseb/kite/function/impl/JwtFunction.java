/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Charsets;
import com.groupeseb.kite.ContextProcessor;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{JWT:objectVariableName}} placeholders by the JWT value corresponding
 * to the object declared in the objectVariables section of the test.
 * It supports nested placeholders.
 * JWT is not signed and use HS256 algorithm.
 * if objectVariableName is a String, then it will be used as is.
 */
@Component
public class JwtFunction extends ObjectFunction {

	/**
	 * There is no easy way to passe from a string representation of a json to its JWT representation (frameworks use map --> string).
	 * So we build it manually.
	 */
	private static final String JWT_HS256_HEADERS = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.";

	JwtFunction() {
		super("JWT");
	}

	@Override
	protected String innerApplyOnObject(JSONObject untransformedJsonObject, ContextProcessor context) {
		String json = context.processPlaceholdersInString(untransformedJsonObject.toJSONString());
		return JWT_HS256_HEADERS + new String(new Base64().encode(json.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
	}

}
