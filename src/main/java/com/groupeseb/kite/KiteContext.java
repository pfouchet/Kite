package com.groupeseb.kite;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Data
public class KiteContext {
	private final Map<String, String> uuids = new HashMap<>();
	private final Map<String, String> locations = new HashMap<>();
	private final Map<String, String> variables = new HashMap<>();
	private final Map<String, String> bodies = new HashMap<>();
	private final Map<String, Object> objectVariables = new HashMap<>();

	public void extend(KiteContext kiteContext) {
		this.uuids.putAll(kiteContext.uuids);
		this.locations.putAll(kiteContext.locations);
		this.variables.putAll(kiteContext.variables);
		this.objectVariables.putAll(kiteContext.objectVariables);
	}

	public void addLocation(String name, String location) {
		locations.put(name, location);
	}

	public void addUUIDs(Map<String, String> uuids) {
		this.uuids.putAll(uuids);
	}

	public void addVariable(String key, String value) {
		this.variables.put(key, value);
	}

	public Object getObjectVariable(String objectName) {
		return this.objectVariables.get(objectName);
	}

	public String getVariableValue(String variableName) {
		return this.variables.get(variableName.trim());
	}

	public String getBody(String objectName) {
		return this.bodies.get(objectName);
	}

	public void addBody(String name, String response) {
		this.bodies.put(name, response);
	}
}
