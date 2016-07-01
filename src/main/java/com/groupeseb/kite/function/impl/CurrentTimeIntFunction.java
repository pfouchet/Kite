package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.ContextProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Function to calculate a an integer current time
 */
@Slf4j
@Component
public class CurrentTimeIntFunction extends AbstractWithOneParameter {

	CurrentTimeIntFunction() {
		super("currentTimeInt");
	}


	@Override
	String apply(String parameter, ContextProcessor contextProcessor) {
		int delta = Integer.parseInt(parameter);
		long timeInt = (System.currentTimeMillis() / 1000) + delta;
		return String.valueOf(timeInt);
	}
}
