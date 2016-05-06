package com.groupeseb.kite.function.impl;

import com.google.common.base.Charsets;
import com.groupeseb.kite.KiteContext;
import com.groupeseb.kite.Json;
import com.groupeseb.kite.function.Function;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.groupeseb.kite.function.impl.DataProvider.getCreationLog;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class JwtFunctionTest {

	private static final String SIMPLE_VALUE = "simpleValue";
	private static final String PROFILE_UID = "profileUid";
	private static final String ROOT_OBJECT_NAME = "authorization";
	private final Function function = new JwtFunction();

	private static String decodeAndExtract(String jwt, String key) throws ParseException {
		return new Json(new String(new Base64().decode(jwt.split("\\.")[1].getBytes(Charsets.UTF_8)),
		                           Charsets.UTF_8)).getString(key);
	}

	@Test
	public void testSimpleObject() throws ParseException {
		KiteContext kiteContext = getCreationLog();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PROFILE_UID, SIMPLE_VALUE);
		kiteContext.getObjectVariables().put(ROOT_OBJECT_NAME, jsonObject);

		String authorization = function.apply(Collections.singletonList(ROOT_OBJECT_NAME), kiteContext);

		assertEquals(decodeAndExtract(authorization, PROFILE_UID), SIMPLE_VALUE);
	}

	@Test
	public void withPlaceholders() throws ParseException {

		KiteContext kiteContext = getCreationLog();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PROFILE_UID, "{{Variable:profileVariable}}");
		kiteContext.getObjectVariables().put(ROOT_OBJECT_NAME, jsonObject);

		kiteContext.addVariable("profileVariable", SIMPLE_VALUE);

		String authorization = function.apply(Collections.singletonList(ROOT_OBJECT_NAME), kiteContext);

		assertEquals(decodeAndExtract(authorization, PROFILE_UID), SIMPLE_VALUE);
	}

}