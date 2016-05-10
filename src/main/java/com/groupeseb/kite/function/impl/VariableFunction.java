/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.function.AbstractWithParameters;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function that replaces {{Variable:variableName}} placeholders by the value the variable named
 * "variableName" in the creationLog
 *
 * @author jcanquelain
 */
@Component
public class VariableFunction extends AbstractWithParameters {
	VariableFunction() {
		super("Variable");
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		String variableName = getUniqueParameter(parameters);
		String variableValue = context.getKiteContext().getVariableValue(variableName);
		return Preconditions.checkNotNull(variableValue, "No value corresponds to variable named [%s]",
				variableName);
	}

}
