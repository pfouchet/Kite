package com.groupeseb.kite;

import com.groupeseb.kite.function.Function;
import com.groupeseb.kite.function.impl.UUIDFunction;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@Data
public class CreationLog {
    private final Map<String, String> uuids = new HashMap<>();
    private final Map<String, String> locations = new HashMap<>();
    private final Map<String, String> variables = new HashMap<>();
    private final Map<String, String> bodies = new HashMap<>();

    private Collection<Function> availableFunctions;

    public CreationLog(Collection<Function> availableFunctions) {
        this.availableFunctions = availableFunctions;
    }

    public void extend(CreationLog creationLog) {
        this.uuids.putAll(creationLog.uuids);
        this.locations.putAll(creationLog.locations);
        this.variables.putAll(creationLog.variables);
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

    public String getVariableValue(String variableName) {
        return this.variables.get(variableName.trim());
    }

    public String getBody(String objectName) {
        return this.bodies.get(objectName);
    }

    private Map<String, String> getEveryUUIDs(String scenario) {
        Pattern uuidPattern = Pattern.compile("\\{\\{" + UUIDFunction.NAME
                + ":(.+?)\\}\\}");
        Matcher uuidMatcher = uuidPattern.matcher(scenario);

        Map<String, String> uuids = new HashMap<>();

        while (uuidMatcher.find()) {
            String name = uuidMatcher.group(1);

            if (!this.getUuids().containsKey(name)) {
                uuids.put(name, UUID.randomUUID().toString());
            }
        }

        return uuids;
    }

    public Function getFunction(String name) {
        for (Function availableFunction : availableFunctions) {
            if (availableFunction.match(name)) {
                return availableFunction;
            }
        }

        return null;
    }

    String executeFunctions(String name, String body) {
        // Function name is escaped for matching, since body is a JSON String
        // (see
        // org.json.simple.JSONObject.toJSONString()), and thus placeholder will
        // have its name
        // escaped in body if it contains JSON special characters

        String escapedFunctionName = JSONObject.escape(name);
        Pattern withoutParameters = Pattern.compile("\\{\\{"
                + escapedFunctionName + "\\}\\}", Pattern.CASE_INSENSITIVE);

        if (withoutParameters.matcher(body).find()) {
            body = withoutParameters.matcher(body).replaceAll(
                    getFunction(name).apply(new ArrayList<String>(), this));
        } else {
            Pattern pattern = Pattern.compile("\\{\\{" + escapedFunctionName
                    + "\\:(.+?)\\}\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(body);

            while (matcher.find()) {
                List<String> parameters = new ArrayList<>();

                for (int i = 1; i <= matcher.groupCount(); ++i) {
                    // If function parameter contains JSON special character,
                    // the are encoded by the
                    // JSON parser. It is necessary to unecape them before using
                    // them in the
                    // function
                    String paramValue = StringEscapeUtils.unescapeJson(matcher
                            .group(i));
                    parameters.add(paramValue);
                }

                String functionResult = getFunction(name).apply(parameters,
                        this);
                // Function result is JSON-escaped before being reinjected in
                // body, to keep body
                // String valid with respect to JSON syntax
                body = body.replace(matcher.group(0),
                        JSONObject.escape(functionResult));
            }
        }
        return body;
    }

    String applyFunctions(String body) {
        String processedBody = new String(body);

        for (Function availableFunction : availableFunctions) {
            processedBody = executeFunctions(availableFunction.getName(),
                    processedBody);
        }

        // 'Timestamp' is not implemented like other functions, because that
        // would not permit to
        // generate the same date for the whole command (since function is
        // called for each
        // placeholder and not one time by)
        processedBody = processedBody.replace("{{Timestamp:Now}}", JSONObject
                .escape(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT
                        .format(new Date())));

        return processedBody;
    }

    public String processPlaceholdersInString(String body) {
        return processPlaceholders(null, body);
    }

    /**
     * Replace placeholders in given body string by applying functions available
     * for this creation log
     * 
     * @param commandName
     *            the name of the command for which placeholders are escaped
     * @param body
     *            the String representation of a JSON object. This String is
     *            expected to be valid with respect to JSON syntax (with escaped
     *            values in key and String values). Placeholders are unescaped
     *            before being processed, and their replacing value is escaped
     *            before being replaced in the returned String
     * @return the initial body with function's placehoders replaced
     */
    public String processPlaceholders(String commandName, String body) {
        String processedBody = new String(body);

        // Assign UUID for current command if needed
        if (commandName != null) {
            processedBody = processedBody
                    .replace("{{" + UUIDFunction.NAME + "}}", "{{"
                            + UUIDFunction.NAME + ":" + commandName + "}}");
        }
        // Update UUIDs list to add the one assigned for current command
        this.uuids.putAll(getEveryUUIDs(processedBody));

        processedBody = applyFunctions(processedBody);
        return processedBody;
    }

    public Object processPlaceholders(Object expected) throws ParseException {
        if (expected instanceof String) {
            return processPlaceholdersInString(expected.toString());
        } else if (expected instanceof Json) {
            Json expectedObject = (Json) expected;
            return new Json(
                    processPlaceholdersInString(expectedObject.toString()));
        } else if (expected instanceof Boolean) {
            return expected;
        } else if (expected instanceof Long) {
            return expected;
        } else if (expected instanceof Double) {
            return expected;
        } else {
            throw new NotImplementedException();
        }
    }

    public void addBody(String name, String response) {
        this.bodies.put(name, response);
    }
}
