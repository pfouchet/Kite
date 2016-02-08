package com.groupeseb.kite.function.impl.lookup;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to transform input Date from source format to target format
 */
@Component
class DateFormatLookupFunction extends AdditionalLookupFunction {

	@Override
	public String getName() {
		return "dateformat";
	}

	@Override
	public String apply(String input, String[] parameters) {
		if (parameters.length != 2) {
			throw new IllegalArgumentException("[dateformat function] expected format : dateformat:yyyy-mm-dd:mm-dd-yyyy");
		}
		String inDateFormat = parameters[0];
		String outDateFormat = parameters[1];

		Date date;
		try {
			date = new SimpleDateFormat(inDateFormat, Locale.getDefault()).parse(input);
		} catch (ParseException e) {
			throw new IllegalStateException("Error to excute " + getName() + " Lookup Function ", e);
		}
		return new SimpleDateFormat(outDateFormat, Locale.getDefault()).format(date);
	}
}
