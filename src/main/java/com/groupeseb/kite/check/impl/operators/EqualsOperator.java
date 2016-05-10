package com.groupeseb.kite.check.impl.operators;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckOperator;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

import static org.testng.Assert.assertEquals;

/**
 * Verifies equality between 2 <b>primitive</b> objects (numbers, string [case-sensitive], booleans)
 *
 * @author mgaudin
 */
@Component
public class EqualsOperator implements ICheckOperator {
	@Override
	public boolean match(String name) {
		return "equals".equalsIgnoreCase(name);
	}

	@Override
	public void apply(@Nullable Object value, @Nullable Object expected, String description, Json parameters) {
		if (value == null || expected == null) {
			assertEquals(value, expected, description);
			return;
		}
		if (value instanceof Number && expected instanceof Number) {
			assertEquals(((Number) value).doubleValue(), ((Number) expected).doubleValue(), description);
			return;
		}

		if (value instanceof Number) {
			try {
				assertEquals(((Number) value).doubleValue(), Double.valueOf(expected.toString()), description);
				return;
			} catch (NumberFormatException ignored) {
			}
		}

		assertEquals(value, expected, description);
	}
}
