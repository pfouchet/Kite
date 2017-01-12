package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * This bean will run all commands defined in a {@link Scenario}
 */
@Slf4j
@Component
public class ScenarioRunner {
	private final CommandRunner commandRunner;
	private final Collection<Function> functions;

	@Autowired
	ScenarioRunner(Collection<Function> functions, CommandRunner commandRunner) {
		this.functions = functions;
		this.commandRunner = commandRunner;
	}

	public KiteContext execute(String fileName, KiteContext kiteContext) throws Exception {
		ContextProcessor context = new ContextProcessor(functions, kiteContext);
		return executeWithContext(new Scenario(fileName), context).getKiteContext();
	}

	public KiteContext execute(String fileName) throws Exception {
		return execute(fileName, new KiteContext());
	}

	public KiteContext execute(InputStream inputStream, KiteContext kiteContext) throws Exception {
		ContextProcessor context = new ContextProcessor(functions, kiteContext);
		return executeWithContext(new Scenario(inputStream), context).getKiteContext();
	}

	public KiteContext execute(InputStream inputStream) throws Exception {
		return execute(inputStream, new KiteContext());
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
			commandRunner.execute(command, context);
		}

		return context;
	}
}
