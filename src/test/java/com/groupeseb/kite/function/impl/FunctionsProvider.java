package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionsProvider {

	public static List<Function> getFunctions() {
		return Arrays.asList(
				(Function) new Base64Function(),
				new JwtFunction(),
				new LocationFunction(),
				new RandomIntegerFunction(),
				new RandomStringFunction(),
				new UUIDFunction(),
				new VariableFunction());
	}

}
