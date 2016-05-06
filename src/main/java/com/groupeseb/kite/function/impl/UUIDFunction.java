/**
 * 
 */
package com.groupeseb.kite.function.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.KiteContext;
import com.groupeseb.kite.function.Function;

/**
 * Function that replaces {{UUID:objectName}} placeholders by the value of the UUID that was
 * generated for command with name "objectName" in creationLog
 * 
 * @author jcanquelain
 *
 */
@Component
public class UUIDFunction extends Function {
	/** Name of this function as it appears in placeholders */
	public final static String NAME = "UUID";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String apply(List<String> parameters, KiteContext kiteContext) {
        Preconditions.checkArgument(parameters.size() == 1, "objectName is needed for [%s] function", NAME);
        String objectName = parameters.get(0);
		String objectUUID = kiteContext.getUuids().get(objectName);
        Preconditions.checkNotNull(objectUUID, "No UUID corresponds to object named [%s]", objectName);
		return objectUUID;
	}
}
