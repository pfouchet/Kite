package com.groupeseb.kite;

import com.google.common.base.Preconditions;
import com.jayway.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * This Helper class was written to make testing Json output easier.
 * Because I write methods lazily, feel free to add new ones if required for your tests
 */
@SuppressWarnings("UnusedDeclaration")
public class Json {
	private final JSONParser parser = new JSONParser();
	@Nullable
	private JSONObject rootObject;

	@Nullable
	private JSONArray rootArray;
	private String json;

	/**
	 * @param r The response to parse
	 * @throws ParseException
	 */
	public Json(Response r) throws ParseException {
		this.json = r.prettyPrint();

		Object root = parser.parse(json);
		if (root instanceof JSONObject) {
			rootObject = (JSONObject) root;
		} else if (root instanceof JSONArray) {
			rootArray = (JSONArray) root;
		}
	}

	/**
	 * @param json The json to parse
	 * @throws ParseException
	 */
	public Json(String json) throws ParseException {
		this.json = json;

		Object root = parser.parse(json);
		if (root instanceof JSONObject) {
			rootObject = (JSONObject) root;
		} else if (root instanceof JSONArray) {
			rootArray = (JSONArray) root;
		}
	}

	protected Json(JSONObject root) {
		rootObject = root;
	}

	protected Json(JSONArray root) {
		rootArray = root;
	}

	/**
	 * In a fluent fashion, return the Json object
	 * corresponding to the selected subtree.
	 *
	 * @param key The key to build the subtree from.
	 * @return A Json object which root is the selected key.
	 */
	@Nullable
	public Json get(String key) {
		Object subTree = getRootObject().get(key);

		if (subTree == null) {
			return null;
		}

		if (subTree instanceof JSONObject) {
			return new Json((JSONObject) subTree);
		} else {
			return new Json((JSONArray) subTree);
		}
	}

	public Object getObject(String key) {
		Object subTree = getRootObject().get(key);

		if (subTree instanceof JSONObject) {
			return new Json((JSONObject) subTree);
		}
		if (subTree instanceof JSONArray) {
			return new Json((JSONArray) subTree);
		}

		return subTree;
	}

	/**
	 * In a fluent fashion, return the Json object
	 * corresponding to the selected subtree.
	 *
	 * @param index The index of the array to build the subtree from.
	 * @return A Json object which root is the selected index.
	 */
	public Json get(Integer index) {
		Object subTree = getRootArray().get(index);

		if (subTree instanceof JSONObject) {
			return new Json((JSONObject) subTree);
		} else {
			return new Json((JSONArray) subTree);
		}
	}

	/**
	 * Get a leaf converted to Long.
	 *
	 * @param key The leaf key
	 */
	@Nullable
	public Long getLong(String key) {
		return (Long) getRootObject().get(key);
	}

	/**
	 * Get a leaf converted to Long.
	 *
	 * @param key The leaf key
	 */
	@Nullable
	public Double getDouble(String key) {
		return (Double) getRootObject().get(key);
	}

	/**
	 * Get a leaf converted to Integer.
	 * <p/>
	 * Warning: If the original data was Long, the
	 * value will be cropped to the maximal
	 * capaticy of Integer.
	 *
	 * @param key The leaf key
	 */
	public int getIntegerOrDefault(String key, int defaultValue) {
		if (exists(key)) {
			return getInteger(key);
		}

		return defaultValue;
	}

	public int getInteger(String key) {
		if (exists(key)) {
			return Integer.parseInt(getRootObject().get(key).toString());
		}

		throw new IndexOutOfBoundsException("Key '" + key + "' was not found.");
	}

	public int getLength() {
		return getRootArray().size();
	}

	public int getLength(String key) {
		Object o = getRootObject().get(key);
		return o == null ? 0 : ((JSONArray) o).size();
	}

	/**
	 * Get a string leaf
	 *
	 * @param key The leaf key
	 */
	@Nullable
	public String getString(String key) {
		return (String) getRootObject().get(key);
	}

	/**
	 * Get a string leaf
	 *
	 * @param key The leaf key
	 */
	@Nullable
	public String formatFieldToString(String key) {
		Object o = getRootObject().get(key);
		if (o instanceof JSONObject) {
			return ((JSONObject) o).toJSONString();
		}
		return o == null ? null : o.toString();
	}

	@Nullable
	public String getString(int index) {
		return (String) getRootArray().get(index);
	}

	/**
	 * Get the JSON string
	 */
	@Override
	public String toString() {
		return rootObject == null ?
				getRootArray().toJSONString() : getRootObject().toJSONString();
	}

	public void checkExistence(String... keys) {
		for (String key : keys) {
			if (!this.exists(key)) {
				throw new IllegalArgumentException("Key not found: " + key);
			}
		}
	}

	public boolean exists(String key) {
		return getRootObject().get(key) != null;
	}

	public Boolean getBooleanOrDefault(String key, Boolean defaultValue) {
		if (exists(key)) {
			return (Boolean) getRootObject().get(key);
		}

		return defaultValue;
	}

	public boolean isIterable() {
		return rootArray != null;
	}

	public <T> Iterator getIterable() {
		return rootArray.iterator();
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> getIterable(String key) {
		Object o = getRootObject().get(key);
		if (o == null) {
			return Collections.emptyList();
		}

		Preconditions.checkArgument(o instanceof Iterable, "Value associated to " + key + " must be a list.");
		return (Iterable<T>) o;
	}

	public Map getMap(String key) {
		JSONObject nonNullObject = getRootObject();
		return nonNullObject.containsKey(key) ?
				(Map) nonNullObject.get(key) : Collections.emptyMap();
	}


	public JSONObject getRootObject() {
		return requireNonNull(rootObject,"rootObject must be not null");
	}

	public JSONArray getRootArray() {
		return requireNonNull(rootArray,"rootArray must be not null");
	}

	public String getStringOrDefault(String key, String defaultValue) {
		if (exists(key)) {
			return requireNonNull(getString(key));
		}

		return defaultValue;
	}

	public Object getObject(int i) {
		return getRootArray().get(i);
	}
}
