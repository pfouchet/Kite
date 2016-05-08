/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.ContextProcessor;
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
	public String apply(List<String> parameters, ContextProcessor context) {
		checkArgument(parameters.size() == 1,
		              "Parameter name is needed for [%s] function", getName());

		String variableName = parameters.get(0);
		Object untransformedJsonObject = checkNotNull(context.getKiteContext().getObjectVariable(variableName),
		                                              "No object corresponds to variable named [%s], do you have the corresponding \"objectVariables\" section in your test?",
		                                              variableName);

		if (untransformedJsonObject instanceof JSONObject) {
			return innerApplyOnObject((JSONObject) untransformedJsonObject, context);
		}
		if (untransformedJsonObject instanceof String) {
			return innerApplyOnString((String) untransformedJsonObject, context);
		}

		throw new NotYetSupportedException("Class not yet supported in ObjectAsStringFunction "
		                                   + untransformedJsonObject.getClass().getSimpleName());
	}

	protected abstract String innerApplyOnObject(JSONObject untransformedJsonObject, ContextProcessor context);

	protected String innerApplyOnString(String rawString, ContextProcessor context) {
		return context.processPlaceholdersInString(rawString);
	}

}
