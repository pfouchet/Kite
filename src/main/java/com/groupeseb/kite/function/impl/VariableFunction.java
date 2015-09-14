/**
 * 
 */
package com.groupeseb.kite.function.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;

/**
 * Function that replaces {{Variable:variableName}} placeholders by the value the variable named
 * "variableName" in the creationLog
 * 
 * @author jcanquelain
 *
 */
@Component
public class VariableFunction extends Function {
	@Override
	public String getName() {
		return "Variable";
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		Preconditions.checkArgument(parameters.size() == 1,
				"variableName is needed for [%s] function", getName());
		String variableName = parameters.get(0);
		String variableValue = creationLog.getVariableValue(variableName);
		Preconditions.checkNotNull(variableValue, "No value corresponds to variable named [%s]",
				variableName);
		return variableValue;
	}

}
