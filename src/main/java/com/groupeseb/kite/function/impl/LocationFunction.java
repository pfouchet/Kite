package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.ContextProcessor;
import org.springframework.stereotype.Component;

/**
 * Function that replaces {{Location:objectName}} placeholders by the full URI of the object
 * identified by <code>objectName</code> in the creationLog
 *
 * @author jcanquelain
 */
@Component
public class LocationFunction extends AbstractWithOneParameter {

	LocationFunction() {
		super("Location");
	}

	@Override
	protected String apply(String parameter, ContextProcessor context) {
		String locationURI = context.getKiteContext().getLocations().get(parameter);
		return Preconditions.checkNotNull(locationURI, "No location corresponds to object named [%s]", parameter);
	}
}
