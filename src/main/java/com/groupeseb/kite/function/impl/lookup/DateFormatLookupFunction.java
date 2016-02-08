package com.groupeseb.kite.function.impl.lookup;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to transform input Date from source format to target format
 */
@Component
class DateFormatLookupFunction extends AdditionalLookupFunction {
	private static final Pattern PATTERN = Pattern.compile("dateformat:\\[(.+?)\\]:\\[(.+?)\\]", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean match(String additionalParameter) {
		return PATTERN.matcher(additionalParameter).matches();
	}

	@Override
	public String apply(String input, String additionalParameter) {
		Matcher matcher = PATTERN.matcher(additionalParameter);
		//noinspection ResultOfMethodCallIgnored
		matcher.matches();
		String inDateFormat = matcher.group(1);
		String outDateFormat = matcher.group(2);

		Date date;
		try {
			date = new SimpleDateFormat(inDateFormat, Locale.getDefault()).parse(input);
		} catch (ParseException e) {
			throw new IllegalStateException("Error to excute dateformat Lookup Function ", e);
		}
		return new SimpleDateFormat(outDateFormat, Locale.getDefault()).format(date);
	}
}
