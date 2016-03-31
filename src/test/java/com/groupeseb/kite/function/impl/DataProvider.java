package com.groupeseb.kite.function.impl;

import com.groupeseb.kite.CreationLog;

import java.util.Arrays;

public class DataProvider {

	public static CreationLog getCreationLog() {
		CreationLog creationLog = new CreationLog(Arrays.asList(new Base64Function(),
		                                                        new JwtFunction(),
		                                                        new LocationFunction(),
		                                                        new RandomInteger(),
		                                                        new RandomString(),
		                                                        new UUIDFunction(),
		                                                        new VariableFunction()));
		return creationLog;
	}

}
