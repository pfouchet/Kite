package com.groupeseb.kite.function.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Function to calculate a random String
 */
@Slf4j
@Component
public class RandomStringFunction extends AbstractWithoutParametersFunction {

	RandomStringFunction() {
		super("RandomString");
	}

	@Override
	public String apply() {
		return UUID.randomUUID().toString();
	}
}
