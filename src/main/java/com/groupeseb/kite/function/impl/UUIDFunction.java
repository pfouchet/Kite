/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{UUID:objectName}} placeholders by the value of the UUID that was
 * generated for command with name "objectName" in creationLog
 *
 * @author jcanquelain
 */
@Component
public class UUIDFunction extends AbstractWithOneParameter {
	/**
	 * Name of this function as it appears in placeholders
	 */
	public static final String NAME = "UUID";

	UUIDFunction() {
		super(NAME);
	}

	@Override
	public String apply(String parameter, ContextProcessor context) {
		String objectUUID = context.getKiteContext().getUuids().get(parameter);
		return Preconditions.checkNotNull(objectUUID, "No UUID corresponds to object named [%s]", parameter);
	}
}
