package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.KiteContext;

import java.util.Arrays;

public final class DataProvider {

	private DataProvider() {
	}

	public static KiteContext getCreationLog() {
		KiteContext kiteContext = new KiteContext(Arrays.asList(new Base64Function(),
		                                                        new JwtFunction(),
		                                                        new LocationFunction(),
		                                                        new RandomInteger(),
		                                                        new RandomString(),
		                                                        new UUIDFunction(),
		                                                        new VariableFunction()));
		return kiteContext;
	}

}
