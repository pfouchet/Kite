package com.groupeseb.kite.check.impl.operators;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckOperator;
import com.groupeseb.kite.exceptions.CheckFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Operator to check the type of evaluate object.
 */
@Component
@Slf4j
public class TypeOfOperator implements ICheckOperator {

	public static final String NUMERIC = "numeric";
	public static final String REGEX = "regex:";
	public static final String VALUE = "value:";
	public static final String DATE = "date:";
	public static final String MAIL = "mail";
	public static final String ALL = "any";

	private static final String EMAIL_REGEX =
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
					+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	@Override
	public boolean match(String name) {
		return "type".equalsIgnoreCase(name);
	}

	/**
	 * apply operator on current object
	 *
	 * @param value       actual value
	 * @param expected    expected value (type pattern)
	 * @param description description of this assertion
	 * @param failonerror Boolean to avoid fail on error
	 * @param parameters  parameters of check
	 * @throws CheckFailException
	 */
	@SuppressWarnings({"UnusedCatchParameter", "SimpleDateFormatWithoutLocale"})
	@Override
	public void apply(@Nullable Object value, @Nullable Object expected, String description, Boolean failonerror, Json parameters) throws CheckFailException {
		validate(value != null, description + ": value is null", failonerror);

		if (expected instanceof String && value != null) {
			String expect = (String) expected;
			if (expect.equalsIgnoreCase(NUMERIC)) {
				validate(value instanceof Number, description, failonerror);
			}
			if (expect.equalsIgnoreCase(MAIL)) {
				Matcher matcher = EMAIL_PATTERN.matcher(value.toString());
				validate(matcher.matches(), description + ": " + value + " didnt' match email pattern", failonerror);
			}
			if (expect.equalsIgnoreCase(ALL)) {
				assertTrue(true, description);
			}
			if (expect.startsWith(REGEX)) {
				String regex = expect.substring(REGEX.length());
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(value.toString());
				validate(matcher.matches(), description + ": " + value + " didnt' match regex : " + regex, failonerror);
			}
			if (expect.startsWith(VALUE)) {
				String expectedValue = expect.substring(VALUE.length());
				validate(value.toString().equals(expectedValue), description + ": value not equals to [" + expectedValue + ']', failonerror);
			}
			if (expect.startsWith(DATE)) {
				String format = expect.substring(DATE.length());
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				try {
					Date date = sdf.parse(value.toString());
					validate(date != null, description + ": " + value + " didnt' match date pattern " + format, failonerror);
				} catch (ParseException parseEx) {
					validate(false, description + ": " + value + " didnt' match date pattern " + format, failonerror);
				}
			}
		}
	}

	/**
	 * Evaluate condition to fail operator or log error depends on failonerror parameter
	 *
	 * @param condition   condition for fail
	 * @param description description of check
	 * @param failonerror boolean for fail on error
	 * @throws CheckFailException
	 */
	private static void validate(Boolean condition, String description, Boolean failonerror) throws CheckFailException {

		if (failonerror) {
			assertTrue(condition, description);
		} else if (!condition) {
			log.error(description);
			throw new CheckFailException(description);
		}
	}

}