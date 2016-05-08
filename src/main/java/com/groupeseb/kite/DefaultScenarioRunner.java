package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;


@Slf4j
@Component
public class DefaultScenarioRunner {
	private final DefaultCommandRunner defaultCommandRunner;
	private final Collection<Function> functions;

	@Autowired
	DefaultScenarioRunner(Collection<Function> functions, DefaultCommandRunner defaultCommandRunner) {
		this.functions = functions;
		this.defaultCommandRunner = defaultCommandRunner;
	}

	KiteContext execute(Scenario scenario, @Nullable KiteContext kiteContext) throws Exception {
		KiteContext nonNullKiteContext = kiteContext == null ? new KiteContext() : kiteContext;
		return executeWithContext(scenario, new ContextProcessor(functions, nonNullKiteContext)).getKiteContext();
	}

	ContextProcessor executeWithContext(Scenario scenario, ContextProcessor context) throws Exception {
		log.info("Parsing {}...", scenario.getFilename());

		KiteContext kiteContext = context.getKiteContext();
		for (Scenario dependency : scenario.getDependencies()) {
			kiteContext.extend(executeWithContext(dependency, context).getKiteContext());
		}

		log.info("Executing {}...", scenario.getFilename());
		log.info("Testing : " + scenario.getDescription() + "...");

		for (Map.Entry<String, Object> entry : scenario.getVariables().entrySet()) {
			kiteContext.addVariable(entry.getKey(), context.applyFunctions(
					entry.getValue().toString(), false));
		}

		kiteContext.getObjectVariables().putAll(scenario.getObjectVariables());

		for (Command command : scenario.getCommands()) {
			defaultCommandRunner.execute(command, context);
		}

		return context;
	}
}
