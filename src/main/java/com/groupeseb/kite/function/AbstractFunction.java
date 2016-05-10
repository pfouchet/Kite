package com.groupeseb.kite.function;

import com.groupeseb.kite.ContextProcessor;
import lombok.Data;

import java.util.List;
import java.util.regex.Matcher;

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
@Data
public abstract class AbstractFunction {
	private final String name;

	protected AbstractFunction(String name) {
		this.name = name;
	}

	public final boolean match(String name) {
		return name.equalsIgnoreCase(this.name);
	}

	public abstract Matcher getMatcher(String input);

	public abstract boolean idWithParameters();

	public abstract String apply(List<String> parameters, ContextProcessor contextProcessor);

	public abstract String apply();
}
