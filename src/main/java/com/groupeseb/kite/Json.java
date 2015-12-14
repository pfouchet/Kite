package com.groupeseb.kite;

import com.google.common.base.Preconditions;
import com.jayway.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This Helper class was written to make testing Json output easier.
 * Because I write methods lazily, feel free to add new ones if required for your tests
 */
@SuppressWarnings("UnusedDeclaration")
public class Json {
    private final JSONParser parser = new JSONParser();
    private JSONObject rootObject;
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
    public Json get(String key) {
        Object subTree = rootObject.get(key);

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
        Object subTree = rootObject.get(key);

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
        Object subTree = rootArray.get(index);

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
    public Long getLong(String key) {
        return (Long) rootObject.get(key);
    }

    /**
     * Get a leaf converted to Long.
     *
     * @param key The leaf key
     */
    public Double getDouble(String key) {
        return (Double) rootObject.get(key);
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
    public Integer getIntegerOrDefault(String key, Integer defaultValue) {
        if (exists(key)) {
            return Integer.valueOf(getLong(key).toString());
        }

        return defaultValue;
    }

    public Integer getInteger(String key) {
        if (exists(key)) {
            return Integer.valueOf(getLong(key).toString());
        }

        throw new IndexOutOfBoundsException("Key '" + key + "' was not found.");
    }

    public Integer getLength() {
        return rootArray.size();
    }

    public Integer getLength(String key) {
        if (rootObject.get(key) == null) {
            return 0;
        }

        return ((JSONArray) rootObject.get(key)).size();
    }

    /**
     * Get a string leaf
     *
     * @param key The leaf key
     */
    public String getString(String key) {
        return (String) rootObject.get(key);
    }

    public String getString(Integer index) {
        return (String) rootArray.get(index);
    }

    /**
     * Get the JSON string
     */
    @Override
    public String toString() {
        if (rootObject != null) {
            return rootObject.toJSONString();
        } else {
            return rootArray.toJSONString();
        }
    }

    public void checkExistence(String... keys) {
        for (String key : keys) {
            if (!this.exists(key)) {
                throw new RuntimeException("Key not found: " + key);
            }
        }
    }

    public Boolean exists(String key) {
        return rootObject.get(key) != null;
    }

    public Boolean getBooleanOrDefault(String key, Boolean defaultValue) {
        if (exists(key)) {
            return (Boolean) rootObject.get(key);
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
        Object o = rootObject.get(key);
        if (o == null) {
            return new ArrayList<>();
        }

        Preconditions.checkArgument(o instanceof Iterable, "Value associated to " + key + " must be a list.");
        return (Iterable<T>) o;
    }

    public Map getMap(String key) {
        return rootObject.containsKey(key) ? (Map) rootObject.get(key) : new HashMap();
    }


    public JSONObject getRootObject() {
        return rootObject;
    }

    public String getStringOrDefault(String key, String defaultValue) {
        if (exists(key)) {
            return getString(key);
        }

        return defaultValue;
    }

    public Object getObject(Integer i) {
        return rootArray.get(i);
    }
}
