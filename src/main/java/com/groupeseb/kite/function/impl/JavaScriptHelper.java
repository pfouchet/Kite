package com.groupeseb.kite.function.impl;

import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.testng.Assert.fail;

/**
 * Class to  excute javadcript value
 * The script can use the var 'inputValue' and must produce the var 'outputValue'.
 */
@Slf4j
public final class JavaScriptHelper {
	private JavaScriptHelper() {
	}

	public static String eval(String script, String inputValue) {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		engine.put("inputValue", inputValue);
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			String message = String.format("Error to excute script : %s\n with inputValue : %s", script, inputValue);
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
