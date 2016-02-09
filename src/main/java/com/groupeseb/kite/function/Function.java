package com.groupeseb.kite.function;

import com.groupeseb.kite.CreationLog;

import java.util.List;

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
 *
 */
public abstract class Function {
    public Boolean match(String name) {
        return name.trim().toUpperCase()
                .equals(this.getName().trim().toUpperCase());
    }

    public abstract String getName();

    public abstract String apply(List<String> parameters,
            CreationLog creationLog);
}
