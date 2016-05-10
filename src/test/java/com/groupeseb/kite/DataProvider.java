package com.groupeseb.kite;

import com.groupeseb.kite.function.impl.FunctionsProvider;

public final class DataProvider {

	private DataProvider() {
	}

	public static ContextProcessor newContextProcessor() {
		return new ContextProcessor(FunctionsProvider.getFunctions(), new KiteContext());
	}

}
