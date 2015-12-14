package com.groupeseb.kite.check.impl.operators;

import static org.testng.Assert.assertTrue;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckOperator;

/**
 * Verifies that 2 objects are different, using natural comparison algorithm (objects must
 * implements {@link Comparable})
 * 
 * @author mgaudin
 *
 */
@Component
public class NotEqualsOperator implements ICheckOperator {
    @Override
    public Boolean match(String name) {
        return "notequals".equalsIgnoreCase(name);
    }


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void apply(Object value, Object expected, String description, Json parameters) {
        Preconditions.checkArgument(value instanceof Comparable, "Using 'equals' or 'notEquals' operators requires Comparable objects.");
        assertTrue(((Comparable) value).compareTo(expected) != 0, description);
    }
}
