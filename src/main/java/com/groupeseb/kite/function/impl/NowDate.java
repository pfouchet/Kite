/**
 *
 */
package com.groupeseb.kite.function.impl;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Function that replaces {{nowDate:yyyy-MM-dd hh:mm:ss}} placeholders by the value of the current date
 */
@Component
public class NowDate extends Function {
	/**
	 * Name of this function as it appears in placeholders
	 */
	private static final String NAME = "NowDate";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String apply(List<String> parameters, CreationLog creationLog) {
		Preconditions.checkArgument(parameters.size() == 1, "accepted format {nowDate:dateFormat}", NAME);
		String dateFormat = parameters.get(0);
		return new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date());
	}
}
