package com.groupeseb.kite.function.impl.lookup;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.fail;

/**
 * Class to load javascript file and execute code.
 * The script can use the var 'inputValue' and must produce the var 'outputValue'.
 */
@Component
class JavaScriptFileLookupFunction extends AdditionalLookupFunction {
	private static final Pattern PATTERN = Pattern.compile("jsFile:(.+?)", Pattern.CASE_INSENSITIVE);

	@Override
	public boolean match(String additionalParameter) {
		return PATTERN.matcher(additionalParameter).matches();
	}

	@Override
	public String apply(String input, String additionalParameter) {
		Matcher matcher = PATTERN.matcher(additionalParameter);
		//noinspection ResultOfMethodCallIgnored
		matcher.matches();
		String jsFile = matcher.group(1);
		URL url = Resources.getResource(jsFile);
		String js = null;
		try {
			js = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			fail("Cannot load file : " + jsFile, e);
		}
		return JavaScriptHelper.eval(js, input);
	}
}
