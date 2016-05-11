package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.ContextProcessor;

import java.util.List;

public abstract class AbstractWithOneParameter extends AbstractWithParameters {
	protected AbstractWithOneParameter(String name) {
		super(name);
	}

	@Override
	public final String apply(List<String> parameters, ContextProcessor contextProcessor) {
		if (parameters.size() == 1) {
			return apply(parameters.get(0), contextProcessor);
		}
		throw new IllegalStateException("Unique parameter is needed for <" + getName() + "> function");
	}

	abstract String apply(String parameter, ContextProcessor contextProcessor);
}
