/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{Variable:variableName}} placeholders by the value the variable named
 * "variableName" in the creationLog
 *
 * @author jcanquelain
 */
@Component
public class VariableFunction extends AbstractWithOneParameter {
	VariableFunction() {
		super("Variable");
	}

	@Override
	public String apply(String parameter, ContextProcessor context) {
		String variableValue = context.getKiteContext().getVariableValue(parameter);
		return Preconditions.checkNotNull(variableValue, "No value corresponds to variable named [%s]",
				parameter);
	}

}
