package com.groupeseb.kite;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.Nullable;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KiteRunner {

	public static KiteContext execute(String filename) throws IOException, ParseException {
		return continueExecute(filename, null);
	}

	public static KiteContext execute(String filename, KiteContext kiteContext) throws IOException, ParseException {
		return continueExecute(filename, kiteContext);
	}

	public static KiteContext continueExecute(String filename, @Nullable KiteContext kiteContext) throws IOException, ParseException {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("kite-beans.xml")) {
			//noinspection OverlyBroadCatchBlock
			try {
				DefaultScenarioRunner bean = context.getBean(DefaultScenarioRunner.class);
				if (kiteContext == null) {
					return bean.execute(new Scenario(filename));
				}
				return bean.execute(new Scenario(filename), kiteContext);
			} catch (Exception e) {
				throw new IllegalStateException("Error on excute kite senario", e);
			}
		}
	}
}
