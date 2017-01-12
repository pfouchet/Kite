package com.groupeseb.kite;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Possible root values :
 * <p/>
 * commands:[CommandObject]
 * dependencies:["urlToAnotherTest"]
 * description:"Description"
 * variables: {
 * "variableName":"variableValue"
 * }
 * objectVariables: {
 * "jwtVariableName":{jwtVariableValueAsJsonObject}
 * }
 * <p/>
 * For variables use, see {@link com.groupeseb.kite.function.impl.VariableFunction}
 * For objectVariables use, see {@link com.groupeseb.kite.function.impl.JwtFunction}
 */
@Getter
public class Scenario {
	public static final String DESCRIPTION_KEY = "description";
	public static final String VARIABLE_KEY = "variables";
	public static final String COMMANDS_KEY = "commands";
	public static final String DEPENDENCIES_KEY = "dependencies";
	public static final String OBJECTS_KEY = "objectVariables";

	private final Collection<Command> commands = new ArrayList<>();
	private final List<Scenario> dependencies = new ArrayList<>();
	private String description;
	private Map<String, Object> variables;
	private Map<String, Object> objectVariables;

	private final String filename;

	/**
	 * @param filename The (class)path to the scenario file.
	 * @throws IOException
	 * @throws ParseException
	 */
	public Scenario(String filename) throws IOException, ParseException {
		this.filename = filename;
		parseScenario(readFixture(filename));
	}

	/**
	 *
	 * @param inputStream a stream with scenario description
	 * @throws IOException
	 * @throws ParseException
	 */
	public Scenario(InputStream inputStream) throws IOException, ParseException {
		this.filename = "direct stream";
		parseScenario(readFixture(inputStream));
	}

	protected static String readFixture(String filename) throws IOException {
		return readFixture(FileHelper.getFileInputStream(filename));
	}

	protected static String readFixture(InputStream stream) throws IOException {
		try (InputStream inputStream = stream) {
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer);
			return writer.toString();
		}
	}

	@SuppressWarnings("unchecked")
	private void parseScenario(String scenario) throws IOException, ParseException {
		Json jsonScenario = new Json(scenario);
		jsonScenario.checkExistence(DESCRIPTION_KEY, COMMANDS_KEY);

		this.description = jsonScenario.getString(DESCRIPTION_KEY);
		this.variables = (Map<String, Object>) jsonScenario.getMap(VARIABLE_KEY);

		this.objectVariables = (Map<String, Object>) jsonScenario.getMap(OBJECTS_KEY);

		for (String dependency : jsonScenario.<String>getIterable(DEPENDENCIES_KEY)) {
			dependencies.add(new Scenario(dependency));
		}

		int commandCount = jsonScenario.getLength(COMMANDS_KEY);
		for (int i = 0; i < commandCount; ++i) {
			Json json = Objects.requireNonNull(jsonScenario.get(COMMANDS_KEY));
			commands.add(new Command(json.get(i)));
		}
	}

	@Override
	public String toString() {
		return this.getFilename() + ':' + this.getDescription();
	}
}
