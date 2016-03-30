/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Charsets;
import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.exceptions.NotYetSupportedException;
import com.groupeseb.kite.function.Function;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Function that replaces {{JWT:variableName}} placeholders by the JWT value corresponding
 * to the object declared in the JWT section of the test.
 * It supports nested placeholders.
 */
@Component
public class JwtFunction extends Function {

	private static final String JWT_HS256_HEADERS = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.";

	@Override
	public String getName() {
		return "JWT";
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		checkArgument(parameters.size() == 1,
		              "JWT name is needed for [%s] function", getName());

		String variableName = parameters.get(0);
		Object untransformedJsonObject = checkNotNull(creationLog.getRawJWT(variableName),
		                                              "No raw jwt corresponds to variable named [%s], do you have the corresponding jwt section in your test?",
		                                              variableName);

		if (untransformedJsonObject instanceof JSONObject) {
			String json = ((JSONObject) untransformedJsonObject).toJSONString();
			json = creationLog.processPlaceholdersInString(json);

			return JWT_HS256_HEADERS + new String(new Base64().encode(json.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
		}

		throw new NotYetSupportedException("Class not yet supported in JWT Function "
		                                   + untransformedJsonObject.getClass().getSimpleName());
	}

}
