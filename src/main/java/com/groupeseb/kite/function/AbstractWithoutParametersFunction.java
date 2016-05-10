package com.groupeseb.kite.function;

import com.groupeseb.kite.ContextProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractWithoutParametersFunction extends AbstractFunction {
	public final Pattern withoutParametersPattern;

	protected AbstractWithoutParametersFunction(String name) {
		super(name);
		withoutParametersPattern = Pattern.compile("\\{\\{" + name + "\\}\\}", Pattern.CASE_INSENSITIVE);
	}

	@Override
	public final Matcher getMatcher(String input) {
		return withoutParametersPattern.matcher(input);
	}

	@Override
	public final String apply(List<String> parameters, ContextProcessor contextProcessor) {
		throw new IllegalStateException("Incorrect method call");
	}

	@Override
	public final boolean isWithParameters() {
		return false;
	}
}
