package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function that replaces {{Location:objectName}} placeholders by the full URI of the object
 * identified by <code>objectName</code> in the creationLog
 *
 * @author jcanquelain
 */
@Component
public class LocationFunction extends Function {
	@Override
	public String getName() {
		return "Location";
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		Preconditions.checkArgument(parameters.size() == 1,
			"objetName parameter is needed for [%s] function", getName());
		String objectName = parameters.get(0);
		String locationURI = creationLog.getLocations().get(objectName);
		return Preconditions.checkNotNull(locationURI, "No location corresponds to object named [%s]", objectName);
	}
}
