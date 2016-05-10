package com.groupeseb.kite.function;

import com.groupeseb.kite.ContextProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for classes allowing to replace placeholders in some {@link String}
 * values, by applying a parameterized (or not) function.
 * <p>
 * Implementations of this abstract class must fulfill following requirements:
 * <ul>
 * <li><b>Name </b>of the function must not contain characters that need to be
 * escaped in strings with respect to JSON specification. These characters are :
 * /,\, ", \b, \f, \n, \r, \t, unicode character with u{4 digit hexa} notation
 * </ul>
 *
 * @author jcanquelain
 */
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
	public final boolean idWithParameters() {
		return true;
	}
}
