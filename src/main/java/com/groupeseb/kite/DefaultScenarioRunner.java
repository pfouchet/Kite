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

	void execute(Scenario scenario) throws Exception {
		execute(scenario, new CreationLog(functions));
	}

	private CreationLog execute(Scenario scenario, CreationLog creationLog) throws Exception {
		log.info("Parsing {}...", scenario.getFilename());

		for (Scenario dependency : scenario.getDependencies()) {
			creationLog.extend(execute(dependency, creationLog));
		}

		log.info("Executing {}...", scenario.getFilename());
		log.info("Testing : " + scenario.getDescription() + "...");

		for (Map.Entry<String, Object> entry : scenario.getVariables().entrySet()) {
			creationLog.addVariable(entry.getKey(), creationLog.applyFunctions(
					entry.getValue().toString(), false));
		}

		creationLog.getObjectVariables().putAll(scenario.getObjectVariables());

		for (Command command : scenario.getCommands()) {
			commandRunner.execute(command, creationLog);
		}

		return creationLog;
	}
}
