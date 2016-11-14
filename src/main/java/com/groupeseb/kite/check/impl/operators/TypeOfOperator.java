package com.groupeseb.kite.check.impl.operators;

import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckOperator;
import com.groupeseb.kite.exceptions.CheckFailException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Operator to check the type of evaluate object.
 * Type Operator need a check rule defined by Api.
 * Api Type :
 * - numeric : check a numeric value
 * - regex : check a defined regex pattern (regex:[_A-Za-z0-9-]*)
 * - value : check a specific value (value:PRO)
 * - date : check a date pattern (date:yyyy-MM-dd'T'HH:mm:ss)
 * - email : check a email value
 * - boolean : check a boolean value
 * - any : only check if value isn't null
 */
@Component
@Slf4j
public class TypeOfOperator implements ICheckOperator {

	public static final String NUMERIC = "numeric";
	public static final String REGEX = "regex";
	public static final String VALUE = "value";
	public static final String DATE = "date";
	public static final String EMAIL = "email";
	public static final String BOOLEAN = "boolean";
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
	 * @param failOnError Boolean to avoid fail on error
	 * @param parameters  parameters of check
	 * @throws CheckFailException
	 */
	@SuppressWarnings({"UnusedCatchParameter", "SimpleDateFormatWithoutLocale"})
	@Override
	public void apply(@Nullable Object value, @Nullable Object expected, String description, Boolean failOnError, Json parameters) throws CheckFailException {
		validate(value != null, description + ": value is null", failOnError);

		if (expected instanceof String && value != null) {
			Type expect = getExpectCase(expected);
			switch (expect.getValue()) {
				case NUMERIC:
					validate(value instanceof Number, description, failOnError);
					break;
				case EMAIL:
					Matcher matcher = EMAIL_PATTERN.matcher(value.toString());
					validate(matcher.matches(), description + ": " + value + " didnt' match email pattern", failOnError);
					break;
				case REGEX:
					String regex = expect.getPattern();
					Pattern pattern = Pattern.compile(regex);
					Matcher matcherReg = pattern.matcher(value.toString());
					validate(matcherReg.matches(), description + ": " + value + " didnt' match regex : " + regex, failOnError);
					break;
				case VALUE:
					String expectedValue = expect.getPattern();
					validate(value.toString().equals(expectedValue), description + ": value not equals to [" + expectedValue + ']', failOnError);
					break;
				case DATE:
					String format = expect.getPattern();
					SimpleDateFormat sdf = new SimpleDateFormat(format);
					try {
						Date date = sdf.parse(value.toString());
						validate(date != null, description + ": " + value + " didnt' match date pattern " + format, failOnError);
					} catch (ParseException parseEx) {
						validate(false, description + ": " + value + " didnt' match date pattern " + format, failOnError);
					}
					break;
				case BOOLEAN:
					validate("true".equalsIgnoreCase(value.toString()) || "false".equalsIgnoreCase(value.toString()),
							description + ": " + value + " didnt' boolean type ", failOnError);
					break;
				case ALL:
					assertTrue(true, description);
					break;
				default:
					fail("Type Operator need an expected rule (See documentation)");
			}

		}
	}

	private static Type getExpectCase(Object expected) {
		if(!(expected instanceof String)) {
			return new Type(ALL, null);
		}
		String expectedStr = (String) expected;
		if(expectedStr.contains(":")) {
			String value = expectedStr.substring(0, expectedStr.indexOf(':'));
			String pattern = expectedStr.substring(expectedStr.indexOf(':')+1);
			return new Type(value, pattern);
		}
		return new Type(expectedStr, null);
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

	@AllArgsConstructor
	@Getter
	private static class Type {
		private String value;
		private String pattern;
	}

}