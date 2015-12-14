/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function that replaces {{UUID:objectName}} placeholders by the value of the UUID that was
 * generated for command with name "objectName" in creationLog
 *
 * @author jcanquelain
 */
@Component
public class UUIDFunction extends Function {
	/**
	 * Name of this function as it appears in placeholders
	 */
	public static final String NAME = "UUID";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		Preconditions.checkArgument(parameters.size() == 1, "objectName is needed for [%s] function", NAME);
		String objectName = parameters.get(0);
		String objectUUID = creationLog.getUuids().get(objectName);
		return Preconditions.checkNotNull(objectUUID, "No UUID corresponds to object named [%s]", objectName);
	}
}
