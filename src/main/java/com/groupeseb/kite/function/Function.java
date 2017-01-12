package com.groupeseb.kite.function;

import com.groupeseb.kite.ContextProcessor;

import java.util.List;
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
public interface Function {

	/**
	 * @return The pattern of function, used to find all occurrence  in string
	 */
	Pattern getPattern();

	/**
	 * @return true if the function need a parameters
	 */
	boolean isWithParameters();

	/**
	 * @param parameters       parameters after function
	 * @param contextProcessor used to access to kiteContext
	 * @return a final value
	 */
	String apply(List<String> parameters, ContextProcessor contextProcessor);

	/**
	 * @return generated value
	 */
	String apply();
}
