package com.groupeseb.kite;

import com.groupeseb.kite.function.AbstractFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;


@Slf4j
@Component
public class ScenarioRunner {
	private final DefaultCommandRunner defaultCommandRunner;
	private final Collection<AbstractFunction> abstractFunctions;

	@Autowired
	ScenarioRunner(Collection<AbstractFunction> abstractFunctions, DefaultCommandRunner defaultCommandRunner) {
		this.abstractFunctions = abstractFunctions;
		this.defaultCommandRunner = defaultCommandRunner;
	}

	public KiteContext execute(String fileName, KiteContext kiteContext) throws Exception {
		ContextProcessor context = new ContextProcessor(abstractFunctions, kiteContext);
		return executeWithContext(new Scenario(fileName), context).getKiteContext();
	}

	public KiteContext execute(String fileName) throws Exception {
		return execute(fileName, new KiteContext());
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
