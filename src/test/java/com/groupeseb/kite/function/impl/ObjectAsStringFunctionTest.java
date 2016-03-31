package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.groupeseb.kite.function.impl.DataProvider.getCreationLog;
import static org.testng.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class ObjectAsStringFunctionTest {

	private static final String SIMPLE_VALUE = "simpleValue";
	private static final String PROFILE_UID_ATTRIBUTE = "profileUid";
	private static final String OBJECT_NAME = "wrapper";
	private final Function function = new ObjectAsStringFunction();

	@Test
	public void testSimpleObject() throws ParseException {
		CreationLog creationLog = getCreationLog();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PROFILE_UID_ATTRIBUTE, SIMPLE_VALUE);
		creationLog.getObjectVariables().put(OBJECT_NAME, jsonObject);

		String jsonAsString = function.apply(Collections.singletonList(OBJECT_NAME), creationLog);

		assertEquals(jsonAsString, "{\"profileUid\":\"simpleValue\"}");
	}

	@Test
	public void testString() throws ParseException {
		CreationLog creationLog = getCreationLog();

		creationLog.getObjectVariables().put(PROFILE_UID_ATTRIBUTE, SIMPLE_VALUE);

		String jsonAsString = function.apply(Collections.singletonList(PROFILE_UID_ATTRIBUTE), creationLog);

		assertEquals(jsonAsString, SIMPLE_VALUE);
	}

	@Test
	public void withFunction() throws ParseException {
		CreationLog creationLog = getCreationLog();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PROFILE_UID_ATTRIBUTE, "{{Variable:simpleVariable}}");
		creationLog.getObjectVariables().put(OBJECT_NAME, jsonObject);

		creationLog.addVariable("simpleVariable", SIMPLE_VALUE);

		String jsonAsString = function.apply(Collections.singletonList(OBJECT_NAME), creationLog);

		assertEquals(jsonAsString, "{\"profileUid\":\"simpleValue\"}");
	}

}