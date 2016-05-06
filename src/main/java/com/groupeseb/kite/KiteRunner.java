package com.groupeseb.kite;

import com.google.common.base.Throwables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KiteRunner {

	public static void execute(Scenario scenario) {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("kite-beans.xml")) {
			//noinspection OverlyBroadCatchBlock
			try {
				context.getBean(DefaultScenarioRunner.class).execute(scenario);
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}
	}

	public static void execute(String filename) throws IOException, ParseException {
		execute(new Scenario(filename));
	}
}
