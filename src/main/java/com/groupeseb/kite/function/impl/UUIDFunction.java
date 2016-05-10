/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.function.AbstractWithParameters;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function that replaces {{UUID:objectName}} placeholders by the value of the UUID that was
 * generated for command with name "objectName" in creationLog
 *
 * @author jcanquelain
 */
@Component
public class UUIDFunction extends AbstractWithParameters {
	/**
	 * Name of this function as it appears in placeholders
	 */
	public static final String NAME = "UUID";

	UUIDFunction() {
		super(NAME);
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		String objectName = getUniqueParameter(parameters);
		String objectUUID = context.getKiteContext().getUuids().get(objectName);
		return Preconditions.checkNotNull(objectUUID, "No UUID corresponds to object named [%s]", objectName);
	}
}
