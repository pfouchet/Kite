package com.groupeseb.kite;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KiteRunner {

	public static ScenarioRunner getInstance() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("kite-beans.xml")) {
			return context.getBean(ScenarioRunner.class);
		}
	}
}
