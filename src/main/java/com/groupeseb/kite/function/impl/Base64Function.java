package com.groupeseb.kite.function.impl;

import com.google.common.base.Charsets;
import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.function.AbstractWithParameters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Encode the given variable value into its Base64 representation.
 */
@Slf4j
@Component
public class Base64Function extends AbstractWithParameters {

	Base64Function() {
		super("Base64");
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		String name = getUniqueParameter(parameters);
		String variableValue = checkNotNull(context.getKiteContext().getVariableValue(name),
				"Variables are not defined or parameter is null");

		return new String(new Base64().encode(variableValue.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
	}
}
