package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.ContextProcessor;

import java.util.List;

public abstract class AbstractWithoutParametersFunction extends AbstractFunction {

	protected AbstractWithoutParametersFunction(String name) {
		super(name, false);
	}

	@Override
	public final String apply(List<String> parameters, ContextProcessor contextProcessor) {
		throw new IllegalStateException("Incorrect method call");
	}
}
