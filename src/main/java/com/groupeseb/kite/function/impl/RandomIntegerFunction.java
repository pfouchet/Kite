package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.function.AbstractWithoutParametersFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Function to calculate a random integer greater than 1
 */
@Slf4j
@Component
public class RandomIntegerFunction extends AbstractWithoutParametersFunction {
	private final Random randomGenerator = new Random();

	RandomIntegerFunction() {
		super("RandomInteger");
	}

	@Override
	public String apply() {
		return Integer.toString(1 + randomGenerator.nextInt());
	}
}
