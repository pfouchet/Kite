package com.groupeseb.kite.function.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.KiteContext;
import com.groupeseb.kite.function.Function;

/**
 * Function that replaces {{Location:objectName}} placeholders by the full URI of the object
 * identified by <code>objectName</code> in the creationLog
 * 
 * @author jcanquelain
 *
 */
@Component
public class LocationFunction extends Function {
	@Override
	public String getName() {
		return "Location";
	}

	@Override
	public String apply(List<String> parameters, KiteContext kiteContext) {
		Preconditions.checkArgument(parameters.size() == 1,
				"objetName parameter is needed for [%s] function", getName());
		String objectName = parameters.get(0);
		String locationURI = kiteContext.getLocations().get(objectName);
		Preconditions.checkNotNull(locationURI, "No location corresponds to object named [%s]",
				objectName);
		return locationURI;
	}
}
