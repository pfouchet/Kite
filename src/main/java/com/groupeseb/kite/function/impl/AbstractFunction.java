package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.function.Function;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public abstract class AbstractFunction implements Function {
	private final String name;
	private final boolean isWithParameters;
	private final Pattern pattern;

	protected AbstractFunction(String name, boolean isWithParameters) {
		this.name = name;
		this.isWithParameters = isWithParameters;
		String additionalRegex = isWithParameters ? ":(.+?)" : "";
		String regex = String.format("\\{\\{%s%s\\}\\}", name, additionalRegex);
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}
}
