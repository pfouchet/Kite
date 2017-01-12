package com.groupeseb.kite.function.impl.lookup;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.testng.Assert.fail;

/**
 * Class to  execute javadcript value
 * The script can use the var 'inputValue' and must produce the var 'outputValue'.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JavaScriptHelper {

	public static String eval(String script, String inputValue) {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		engine.put("inputValue", inputValue);
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			String message = String.format("Error to execute script : %s\n with inputValue : %s", script, inputValue);
			fail(message, e);
		}
		Object outputValue = engine.get("outputValue");
		if (outputValue == null) {
			fail("outputValue cannot be null");
		}
		String result = outputValue.toString();
		log.debug("eval, \nscript= {}, \ninputValue={} \n--> result={}", script, inputValue, result);
		return result;
	}
}
