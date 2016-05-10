package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.function.AbstractWithParameters;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function that replaces {{Location:objectName}} placeholders by the full URI of the object
 * identified by <code>objectName</code> in the creationLog
 *
 * @author jcanquelain
 */
@Component
public class LocationFunction extends AbstractWithParameters {

	LocationFunction() {
		super("Location");
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		String objectName = getUniqueParameter(parameters);
		String locationURI = context.getKiteContext().getLocations().get(objectName);
		return Preconditions.checkNotNull(locationURI, "No location corresponds to object named [%s]", objectName);
	}
}
