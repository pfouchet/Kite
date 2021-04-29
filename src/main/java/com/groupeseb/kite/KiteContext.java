package com.groupeseb.kite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is the context hold by an entire test file. This context will keep track of:
 * <p>
 * uids generated with {{UUID}} command.
 * locations generated with automaticCheck : true and setted name.
 * variables generated with "variables" section.
 * bodies generated with setted name.
 * objectVariables generated with "objectVariables" section.
 * services added manually before test executing
 * <p>
 * It can be twicked beforehand so that specific values maybe added by calling api.
 * <p>
 * The latest addition was to allow authorization during automaticCheck. Since authorization can be anything from simpleAuth to JWT authorization,
 * header that should be used can be customized with com.groupeseb.kite.KiteContext#authorizationHeaderNameForAutomaticCheck attribute.
 */
@NoArgsConstructor
@Data
@Slf4j
public class KiteContext {
	private final Map<String, String> uuids = new HashMap<>();
	private final Map<String, String> locations = new HashMap<>();
	private final Map<String, String> variables = new HashMap<>();
	private final Map<String, String> bodies = new HashMap<>();
	private final Map<String, Object> objectVariables = new HashMap<>();
	private Map<String, Service> services = new HashMap<>();

	/**
	 * This will be the header used when doing an automatic resource check.
	 */
	private String authorizationHeaderNameForAutomaticCheck = null;

	/**
	 * Define the key of the service used by default for all requests that not set "service" param.
	 */
	private String defaultServiceKey = null;

	private static final ObjectMapper OBJECT_MAPPER = initObjectMapper();

	static ObjectMapper initObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	/**
	 * Add new values to current context.
	 *
	 * @param kiteContext {@link KiteContext} with additional values.
	 */
	public void extend(KiteContext kiteContext) {
		this.uuids.putAll(kiteContext.uuids);
		this.locations.putAll(kiteContext.locations);
		this.variables.putAll(kiteContext.variables);
		this.objectVariables.putAll(kiteContext.objectVariables);
		this.services.putAll(kiteContext.services);
		this.authorizationHeaderNameForAutomaticCheck = kiteContext.authorizationHeaderNameForAutomaticCheck;
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
		return checkAndGet(objectVariables, "ObjectVariables", objectName);
	}

	public String getVariableValue(String variableName) {
		return checkAndGet(variables, "Variables", variableName.trim());
	}

	/**
	 * @param objectName name of payloads
	 * @return payload non null and not empty
	 */
	public String getBody(String objectName) {
		return checkAndGet(bodies, "Payloads", objectName);
	}

	public <T> T getBodyAs(String objectName, Class<T> clazz) throws IOException {
		return OBJECT_MAPPER.readValue(getBody(objectName), clazz);
	}

	public void addBody(String name, String response) {
		this.bodies.put(name, response);
	}

	public void addBodyAsJsonString(String name, Object object) throws JsonProcessingException {
		this.bodies.put(name, OBJECT_MAPPER.writeValueAsString(object));
	}

	/**
	 * Add a new service to existing services in context.
	 *
	 * @param key     Key of service for map.
	 * @param service Instance of {@link Service}.
	 */
	public void addService(String key, Service service) {
		this.services.put(key, service);
	}

	/**
	 * Set services map into context.
	 *
	 * @param services Map of services that will be added into context.
	 */
	public void setServices(Map<String, Service> services) {
		this.services = services;
	}

	/**
	 * Retrieve a {@link Service} instance from services in context.
	 *
	 * @param key Key of requested service.
	 * @return The corresponding instance of {@link Service}.
	 */
	public Service getService(String key) {
		return this.services.get(key);
	}

	/**
	 * Retrieve a {@link Service} configured as default in this Kite context.
	 *
	 * @return The default service.
	 */
	public Service getDefaultService() {
		if (defaultServiceKey == null) {
			log.warn("defaultServiceKey parameter is not set for this context");
			return null;
		}

		Service defaultService = this.services.get(defaultServiceKey);
		if (defaultService == null) {
			throw new IllegalArgumentException(String.format("Configured default service %s is not available", defaultServiceKey));
		}

		return defaultService;
	}

	private static <T> T checkAndGet(Map<String, T> map, String mapName, String key) {
		return Objects.requireNonNull(
				map.get(key),
				String.format("Missing <%s> in %s, available keys : %s", key, mapName, map.keySet()));
	}
}
