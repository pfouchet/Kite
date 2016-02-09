package com.groupeseb.kite.function.impl.lookup;

/**
 * Class to matches additional lookup function.
 * Example for lookup {{Lookup:test01.$:functionName:params}} the value 'functionName:params' will be tested by all
 * additionalLookupFunctions to enrich lookup value
 *
 */
public abstract class AdditionalLookupFunction {
	public abstract boolean match(String additionalParameter);

	public abstract String apply(String input, String additionalParameter);
}
