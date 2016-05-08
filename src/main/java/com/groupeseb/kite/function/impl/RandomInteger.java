package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.ContextProcessor;
import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class RandomInteger extends Function {
	private final Random randomGenerator = new Random();

	public String getName() {
		return "RandomInteger";
	}

	@Override
	public String apply(List<String> parameters, ContextProcessor context) {
		int intGenerated = randomGenerator.nextInt();
		while (intGenerated < 0) {
			intGenerated = randomGenerator.nextInt();
		}

		return Integer.toString(intGenerated);
	}
}
