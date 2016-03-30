/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.exceptions.NotYetSupportedException;
import com.groupeseb.kite.function.Function;
import org.json.simple.JSONObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract function which extract Object from CreationLog and allows
 * subclasses to customize result before returning it.
 */
public abstract class ObjectFunction extends Function {

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		checkArgument(parameters.size() == 1,
		              "Parameter name is needed for [%s] function", getName());

		String variableName = parameters.get(0);
		Object untransformedJsonObject = checkNotNull(creationLog.getObjectVariable(variableName),
		                                              "No object corresponds to variable named [%s], do you have the corresponding \"objectVariables\" section in your test?",
		                                              variableName);

		if (untransformedJsonObject instanceof JSONObject) {
			return innerApply((JSONObject) untransformedJsonObject, creationLog);
		}

		throw new NotYetSupportedException("Class not yet supported in ObjectAsStringFunction "
		                                   + untransformedJsonObject.getClass().getSimpleName());
	}

	protected abstract String innerApply(JSONObject untransformedJsonObject, CreationLog creationLog);

}
