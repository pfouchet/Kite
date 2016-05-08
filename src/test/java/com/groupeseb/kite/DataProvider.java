package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import com.groupeseb.kite.function.impl.Base64Function;
import com.groupeseb.kite.function.impl.JwtFunction;
import com.groupeseb.kite.function.impl.LocationFunction;
import com.groupeseb.kite.function.impl.RandomInteger;
import com.groupeseb.kite.function.impl.RandomString;
import com.groupeseb.kite.function.impl.UUIDFunction;
import com.groupeseb.kite.function.impl.VariableFunction;

import java.util.Arrays;
import java.util.List;

public final class DataProvider {

	private DataProvider() {
	}

	public static ContextProcessor newInternalContext() {
		List<Function> functions = Arrays.asList(new Base64Function(),
				new JwtFunction(),
				new LocationFunction(),
				new RandomInteger(),
				new RandomString(),
				new UUIDFunction(),
				new VariableFunction());

		return new ContextProcessor(functions, new KiteContext());
	}

}
