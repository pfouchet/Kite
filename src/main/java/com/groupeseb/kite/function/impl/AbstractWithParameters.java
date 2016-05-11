package com.groupeseb.kite.function.impl;

public abstract class AbstractWithParameters extends AbstractFunction {
	protected AbstractWithParameters(String name) {
		super(name, true);
	}

	@Override
	public final String apply() {
		throw new IllegalStateException("Incorrect method call");
	}
}
