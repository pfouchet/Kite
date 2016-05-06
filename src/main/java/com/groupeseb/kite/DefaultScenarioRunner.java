package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;


@Slf4j
@Component
public class DefaultScenarioRunner {
	private final ICommandRunner commandRunner;
	private final Collection<Function> functions;

	@Autowired
	DefaultScenarioRunner(Collection<Function> functions, ICommandRunner commandRunner) {
		this.functions = functions;
		this.commandRunner = commandRunner;
	}

	KiteContext execute(Scenario scenario) throws Exception {
		return executeWithContext(scenario, new KiteContext(functions));
	}

	KiteContext execute(Scenario scenario, KiteContext kiteContext) throws Exception {
		return executeWithContext(scenario, kiteContext);
	}

	private KiteContext executeWithContext(Scenario scenario, KiteContext kiteContext) throws Exception {
		log.info("Parsing {}...", scenario.getFilename());

		for (Scenario dependency : scenario.getDependencies()) {
			kiteContext.extend(executeWithContext(dependency, kiteContext));
		}

		log.info("Executing {}...", scenario.getFilename());
		log.info("Testing : " + scenario.getDescription() + "...");

		for (Map.Entry<String, Object> entry : scenario.getVariables().entrySet()) {
			kiteContext.addVariable(entry.getKey(), kiteContext.applyFunctions(
					entry.getValue().toString(), false));
		}

		kiteContext.getObjectVariables().putAll(scenario.getObjectVariables());

		for (Command command : scenario.getCommands()) {
			commandRunner.execute(command, kiteContext);
		}

		return kiteContext;
	}
}
