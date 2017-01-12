# Kite
Kite Integration Testing Environment

KITE (Kite Integration Test Environment) is a both a testing scenario language specification and a reference implementation of the runner. It allows one to express a REST api test suite using JSON format.

## How to use

To use KITE, simply add the following Maven dependency :

```xml
<dependency>
    <groupId>com.groupeseb</groupId>
    <artifactId>kite</artifactId>
    <version>3.14</version>
    <scope>test</scope>
</dependency>
```
Then use this sample code to run your scenario :

```java
Scenario scenario = new Scenario("myScenario.json");
new DefaultScenarioRunner().execute(scenario);
```

Alternative way :

```java
Scenario scenario = new Scenario(inputStream);
new DefaultScenarioRunner().execute(scenario);
```

The last version is useful when scenario are built during runtime or are not provided as bare files.

## How to write KITE scenario

A KITE scenario is a JSON file containing the following fields :

### description
The description of what the test does

### dependencies
The ordered list of scenarios to execute prior to the test.

### variables
The dictionary of variables. It may contain placeholders.

### objectVariables

The dictionary of objects. It may contain any placeholders and can have nested structure (variables section cannot).
For the moment only *JWT* placeholder has been tested with this section.

### commands
The ordered list of command to execute during the test

For instance :
```json
{
  "description": "Check that nobody can access this endpoint without authorizations",
  "dependencies": [
    "createUser.json"
  ],
  "variables": {
    "login": "John",
    "badPassword": "1234",
    "goodPassword": "N#2B5&jcP5z5KCvs"
  },
  "objectVariables": {
    "authorization": {
      "id": 1,
      "isStaff": true
    }
  },
  "commands": []
}
```

## Placeholders

To make the development easier and minimal, some placeholder are available and will be replaced at runtime wherever you put it.

Placeholders are scoped to the current Scenario context (it contains prepared context, dependencies and current test) and may have up to 3 arguments, separated by ':' character.

### Base 64

{{Base64:aTitle}} will produce 'aTitle' with Base64 encoding.

### currentTimeInt

{{currentTimeInt}} will produce the current time in seconds.

### RandomString

{{RandomString}} will produce a UUID4.

### RandomInteger

{{RandomInteger}} will produce a random integer between 1 and Max Int.

### Variables

{{Variable:MyVariableName}} will produce value where value is the value defined in the variable node.

### UUID 

{{UUID}} will produce a random UUID and associate it with the current object of the POST request. 
This placeholder is DEPRECATED. Preferring way is {{RandomString}} with variable.

### Named UUID 

{{UUID:User01}} will produce the UUID associated with the object named User01. 
This placeholder is DEPRECATED. Preferring way is {{RandomString}} with variable.

Note : the object User01 must have been created before referenced.

### JWT 

{{JWT:authorization}} will produce an unsigned [JWT](http://www.jwt.io/) for the object found in the objectVariables section.

### URI

{{Location:User01}} will produce the full URI of the object named User01.

Note : the object User01 must have been created before referenced.

### Lookup

{{Lookup:User01.title}} will produce the value matching the jsonpath inside the object named User01.

### Lookup with inlined javascript

Inline javascript can be executed in a Lookup placeholder. It uses second and third argument for configuration :

```
{{Lookup:<registeredName>.<path>:js:outputValue=<InlinedJSScript>}}
```

*outputValue* MUST be set as it will be used for this placeholder.
Special value *inputValue* represents the looked value and is automatically added to the js context.

#### Example

```
{{Lookup:User01.title:js:outputValue=inputValue.concat('more')}}
```

This script will extract the title attribute of User01 and produces the concatenation of title with 'more' string. Any js can be executed but outputValue must be set.

Note : the object User01 must have been created before referenced.

### Lookup with file based javascript

Javascript defined in a file can be executed in a Lookup placeholder. It uses second and third argument for configuration :

```
{{Lookup:<registeredName>.<path>:jsfile:<path/to/script.js>}}
```

*outputValue* MUST be set as it will be used for this placeholder.
Special value *inputValue* represents the looked value and is automatically added to the js context.

### Example

```
{{Lookup:User01.title:jsfile:concatMore.js}}
```

This script will extract title attribute from User01 and apply concatMore.js script.

Note : the object User01 must have been created before referenced.

## Command node

A command corresponds to a HTTP command. It contains the following fields :

### Attributes

#### description
The description of the command performed.

#### verb
The HTTP verb to use [GET, POST, GET, DELETE, HEAD, PUT].

#### uri
The URI to perform the HTTP operation against.
Uri can be a path with */endpoint* format or can be a special value {{Location:existingName}}.

#### body
The body of the HTTP operation. It may contain placeholders.

#### headers
The dictionary of the header values to use

#### expectedStatus
The expected response status of the command.

#### wait
The number of milliseconds to wait before executing the command.

#### name
Usable during a POST, it allow to name a created resource to use it later.

#### automaticCheck
Boolean value available to POST and PUT only. If set to true (default value) then the header Location of the response will be fetched.
If name attribute is set, returned payload will be saved under this name for further use and will be available through {{Location:}} and {{Lookup:}}.
This feature might need further authentication since Location header GET maybe a protected resource. To circumvent this issue, Kite library offers a way to let Api caller defines 
* the header name that should be used (since authentication can be anything from basicAuth to JWT auth) through the KiteContext#authorizationHeaderNameForAutomaticCheck attribute and 
* the header value defined as a variable ("variables" section) called internalCheckFullyAuthenticated.

### POST

During a POST, the default expected status is 201.

Sample scenario :

```json
{
    "description": "Create a minimal recipe (MinimalRecipe01)",
    "commands": [
        {
            "verb": "POST",
            "uri": "/recipes",
            "name": "MinimalRecipe",
            "body": {
                "title": "Blinis au saumon",
                "shortTitle": "Blinis",
                "lang": { "href": "/languages/fr_FR" }
            }
        }
    ]
}
```

### GET

During a GET, the default expectedStatus is 200.

Sample scenario :

```json
{
    "description": "Create evaluations and use the search endpoint to get them back",
    "dependencies": [
        "scenarios/evaluations.json"
    ],
    "commands": [
        {
            "verb": "GET",
            "uri": "/profiles/{{UUID:MinimalProfile01}}/evaluations",
            "description": "Get user evaluations",
            "checks": [
                {
                    "field": "content",
                    "method": "length",
                    "operator": "gt",
                    "expected": 0
                }
            ]
        },
        {
            "verb": "GET",
            "uri": "/profiles/{{UUID:MinimalProfile01}}/evaluations?recipeId={{UUID:MinimalRecipe01}}",
            "description": "Get recipe evaluation (by user)",
            "checks": [
                {
                    "field": "content",
                    "method": "length",
                    "operator": "gt",
                    "expected": 0
                }
            ]
        }
    ]
}
```

### PUT

During a PUT, the default expected status is 204.

### PATCH

During a PATCH, the default expected status is 201.

## Check node

Check node is mainly composed by field, method, operator and expected.

### "Field" attribute

This field defines the complete json path which should be verified.
It is jsonpath compliant i.e dot notation must be used (as content.a.b).
Array exploration is possible and use [] characters.
Between those brackets, digit, wildcard or jsonpath boolean expression can be used :
* content[0].title : title of the first element.
* content[\*].title : an array containing all title from all content.
* content[?(@title=='TITLE_1')] : an array of content matching the condition.
* content[?(@title=='TITLE_1')].id : an array of id coming from content matching the condition.

### Expected value

This field defines the expected value.

### Methods

Methods apply a transformation on the "field" value coming from the json object. The result will be compared to the expected value using the given operator.

#### exists
Return true if the field was found, false otherwise. Empty array or null value match the *exist* condition.

```json
{
  "field": "content",
  "method": "exists",
  "expected": true
}
```

#### length
Return the length of the specified list field

```json
{
  "field": "content",
  "method": "length",
  "expected": 2
}
```

#### contains
Return true if all the specified values were found in the specified field.
This method needs another attribute called "parameters" which must be an array of simple type (numeric, string...)

Example : 


```json
{
  "field": "content[*].arrayOfString",
  "method": "contains",
  "parameters": [
    "DEFAULT"
  ],
  "expected":true
}
```

#### nop
Return the field value. This method is the default one and should not be specified when used.

Example : 

```json
{
  "field": "content[0].title",
  "expected":"TITLE_1"
}
```

### Operators

Operators define assertion which must be verified by expected and actual values.

#### equals 

Return true if the expected value and the actual value are equals, false otherwise. This operator is the default one and should not be specified when used.

#### notequals

Return false if the expected value and the actual value are equals, true otherwise.

#### gt 

Return true if the actual value is greater than the expected, false otherwise.

#### jsonEquals 

Return true if expected value and actual value are equals, accordingly to specified mode. json comparison ignore attribute order.
Possible mode are :
* STRICT : array and attribute order do matter. Comparison is strict.
* LENIENT : array order does not matter, additional attributes are ignored.
* NON_EXTENSIBLE : array order does not matter, additional attributes are forbidden.
* STRICT_ORDER : array order does matter, additional attributes are ignored.

```json
TODO need example
```

#### type

Return true if the expected type match the type of the actual value. This operator MUST be used with the nop method. 
expected values must pick of the values defined in the next section.

Example 

```json
{
  "field": "content[*].yield.quantity",
  "operator": "type",
  "expected": "numeric",
  "failOnError": true
}
```

if failOnError (this keyword can only be used with type operator) is set, test will be in failure otherwise a log will be produced and other checks will continue.

##### Available expected values

* numeric : The value is coercible to numeric value.
* boolean : The value is "true" or "false" whether it is a String or a Boolean value.
* any : The field exists (empty array and null value work).
* date:*pattern* :The value match the given pattern (Example : date:yyyy-MM-dd'T'HH:mm:ss).
* email : The value matches the email pattern.
* value:*value* : The field has the specified value.
* regex:*pattern* : the value matches the given pattern.

## General Architecture

Kite framework is composed by Runners, Scenario and Context and use Spring.

Entrypoint of the library is the com.groupeseb.kite.KiteRunner class which wires every classes together.
For more details, see the internal documentation.
