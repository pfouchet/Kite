package com.groupeseb.kite.check;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.impl.operators.JsonEqualsOperator;
import com.groupeseb.kite.exceptions.CheckFailException;

/**
 * An operator for comparison between actual and expected values.
 * <p>
 * Specific parameters of check ({@link Check#getParameters()}) must be provided (currently used
 * only by {@link JsonEqualsOperator})
 * <p>
 * A description of comparison can be provided
 *
 * @author mgaudin
 */
public interface ICheckOperator {

	/**
	 * Verifies if operator name specified in check ({@link Check#getOperatorName()}) matches this
	 * operator
	 *
	 * @param name method name
	 * @return true if name matches (should be case-insensitive)
	 */
	boolean match(String name);

	/**
	 * Verify assertion with operator
	 *
	 * @param value       actual value
	 * @param expected    expected value
	 * @param description description of this assertion
	 * @param failonerror boolean to avoid fail on error
	 * @param parameters  parameters of check
	 */
	void apply(Object value, Object expected, String description, Boolean failonerror, Json parameters) throws CheckFailException;
}
