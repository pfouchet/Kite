package com.groupeseb.kite.check.impl.operators;

import com.google.common.base.Preconditions;
import com.groupeseb.kite.Json;
import com.groupeseb.kite.check.ICheckOperator;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.EnumUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.stereotype.Component;
import org.testng.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * This operator verify similarity between 2 complex JSON objects (<b>ignoring formatting and order
 * between attributes</b>).
 * <p>
 * When using this operator you must define a 'mode' parameter, using one of values defined in
 * {@link JSONCompareMode} enumerator :
 * <ul>
 * <li><b>STRICT</b>: the assertion is verified is JSON are strictly equivalent (same attributes and
 * elements in same order in arrays).
 * <li><b>LENIENT</b>: the assertion is verified even if elements in array are not in same order
 * and/or actual value has some additional attributes.
 * <li><b>NON_EXTENSIBLE</b> the assertion is verified even if elements in array are not in same
 * order (additional attributes are not accepted).
 * <li><b>STRICT_ORDER</b> the assertion is verified even if actual value has some additional
 * attributes (order in array is important)
 * </ul>
 * 
 * Examples with expected value <code>[{"f1":"v1","f2":"v2"},{"f3":"v3"}]</code>:
 * <ul>
 * <li><b>MATCH EVERYTIME</b>:
 * <ul>
 * <li>
 * <code>[{"f1":"v1","f2":"v2"},{"f3":"v3"}] <li> [{"<b>f2</b>":"v2","<b>f1</b>": "v1"} ,    {"f3":  "v3"}]</code>
 * </ul>
 * <li><b>MATCHES STRICT_ORDER and LENIENT</b>:
 * <ul>
 * <li>
 * <code>[{"f1":"v1","f2":"v2",<b>"foo":"bar"</b>},{"f3":"v3",<b>"foo":"bar"</b>}]</code>
 * </ul>
 * <li><b>MATCHES NON_EXTENSIBLE and LENIENT</b>:
 * <ul>
 * <li>
 * <code>[<b>{"f3":"v3"}</b>,{"f1":"v1","f2":"v2"}]</code>
 * </ul>
 * <li><b>MATCHES ONLY LENIENT</b>:
 * <ul>
 * <li>
 * <code>[<b>{"f3":"v3","foo":"bar"}</b>,{"f1":"v1","f2":"v2",<b>"foo":"bar"</b>}]</code>
 * </ul>
 * <li><b>NEVER MATCH</b>:
 * <ul>
 * <li> <code>[{"f1":"v1"<b>}</b>,{"f3":"v3"}]</code>
 * <li>
 * <code>[{"f1":"v1","f2":"<b>v5</b>"},{"f3":"v3"}]</code>
 * </ul>
 * 
 * 
 * </ul>
 * 
 * @author fpiai
 *
 */
// TODO faut-il remplacer 'one of values defined in {@link JSONCompareMode} enumerator' par 'one of
// following values'?
@Component
public class JsonEqualsOperator implements ICheckOperator {

	@Override
	public Boolean match(String name) {
		return "jsonEquals".equalsIgnoreCase(name);
	}

	@Override
	public void apply(Object value, Object expected, String description, Json parameters) {
		try {
			Object modeString = parameters.getString("mode");
			Preconditions.checkArgument(modeString != null && EnumUtils.isValidEnum(JSONCompareMode.class, (String) modeString),
					"%s check: specify 'mode' for jsonEquals operator. Available modes are: %s",
					description, getAvailableJSONCompareMode());

			Preconditions.checkArgument(expected instanceof Json,
							"%s check: A JSONEquals operator is used, expected object must be a JSON complex object",
							description);
			Assert.assertTrue(value instanceof JSONObject || value instanceof JSONArray,
					String.format(
							"%s check: A JSONEquals operator is used, but resulting object is not JSON: %s",
							description, value));

			/**
			 * TODO we are forced to deserialize JSON (toString below) because of object type
			 * incompatibility <br/>
			 * <ul>
			 * <li>expected JSON in file are deserialized as "Json" object, a KITE internal wrapper
			 * for org.json.simple.JSONArray/JSONObject
			 * <li>actual found values are net.minidev.json.JSONObject/JSONArray
			 * <li>JSONAssert expects org.json.JSONArray/JSONObject
			 * </ul>
			 * I don't know why these objects are used in KITE and why there is this difference
			 * between expected and actual, anyway this simple workaround works correctly (it should
			 * not cause big performance issues) and avoids refactoring deeply the framework.
			 */
			JSONAssert.assertEquals(expected.toString(), value.toString(),
					JSONCompareMode.valueOf((String) modeString));
		} catch (JSONException ignored) {
			throw new RuntimeException(String.format(
							"%s check: Malformed JSON to compare. Should never happen (a check is made before). Value: %s , expected: %s ",
							description, value, expected));
		} catch (AssertionError e) {
			// JSONAssert does not have a description parameter so I had to do this hack
			AssertionError assertionError = new AssertionError(String.format(
					"Assertion failed in %s check: %s", description,
					e.getMessage()), e);
			assertionError.setStackTrace(e.getStackTrace());
			throw assertionError;
		}

	}

	/**
	 * Returns a list of available JSON compare mode ( {@link JSONCompareMode#values()} cannot be
	 * used because toString returns specific class attributes instead of enum 'names'
	 * {@link JSONCompareMode#name()})
	 * 
	 * @return
	 */
	private static List<String> getAvailableJSONCompareMode() {
		List<String> availableMethods = new LinkedList<>();
		for (JSONCompareMode aMode : JSONCompareMode.values()){
			availableMethods.add(aMode.name());
		}
		return availableMethods;
	}

}
