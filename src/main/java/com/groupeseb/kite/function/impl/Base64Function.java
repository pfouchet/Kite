package com.groupeseb.kite.function.impl;

import com.google.common.base.Charsets;
import com.groupeseb.kite.KiteContext;
import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Encode the given variable value into its Base64 representation.
 */
@Slf4j
@Component
public class Base64Function extends Function {
	@Override
	public String getName() {
		return "Base64";
	}

	@Override
	public String apply(List<String> parameters, KiteContext kiteContext) {
		checkArgument(parameters.size() == 1, "Exactly one parameter is needed");

		String variableValue = checkNotNull(kiteContext.getVariableValue(parameters.get(0)),
		                                    "Variables are not defined or parameter is null");

		return new String(new Base64().encode(variableValue.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
	}
}
