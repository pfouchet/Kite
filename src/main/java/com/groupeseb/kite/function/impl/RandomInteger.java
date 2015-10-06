package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;
import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class RandomInteger extends Function {
	private final Random randomGenerator = new Random();

	public String getName() {
		return "RandomInteger";
	}

	public String apply(List<String> parameters, CreationLog creationLog) {
		if (CollectionUtils.isEmpty(parameters)) {
			return Integer.toString(randomGenerator.nextInt());
		} else {
			return Integer.toString(randomGenerator.nextInt(Integer.parseInt(parameters.get(0))));
		}
	}
}
