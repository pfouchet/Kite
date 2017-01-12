package com.groupeseb.kite;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Wire everything together to execute Scenario.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KiteRunner {
	public static ScenarioRunner getInstance() {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(KiteAppConfig.class)) {
			return context.getBean(ScenarioRunner.class);
		}
	}
}
