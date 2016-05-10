package com.groupeseb.kite.function;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractWithParameters extends AbstractFunction {
	public final Pattern witParametersPattern;

	protected AbstractWithParameters(String name) {
		super(name);
		witParametersPattern = Pattern.compile("\\{\\{" + name + ":(.+?)\\}\\}", Pattern.CASE_INSENSITIVE);
	}

	@Override
	public final Matcher getMatcher(String input) {
		return witParametersPattern.matcher(input);
	}

	protected final String getUniqueParameter(List<String> parameters) {
		if (parameters.size() == 1) {
			return parameters.get(0);
		}
		throw new IllegalStateException("Unique parameter is needed for <" + getName() + "> function");
	}

	@Override
	public final String apply() {
		throw new IllegalStateException("Incorrect method call");
	}

	@Override
	public final boolean isWithParameters() {
		return true;
	}
}
