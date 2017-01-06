# Kite
Kite Integration Testing Environment

KITE (Kite Integration Test Environment) is a both a testing scenario language specification and a reference implementation of the runner.

## How to use

To use KITE, simply add the following Maven dependency :

<dependency>
    <groupId>com.groupeseb</groupId>
    <artifactId>kite</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
Then use this sample code to run your scenario :

Scenario scenario = new Scenario("myScenario.json");
new DefaultScenarioRunner().execute(scenario);
Warning : Your scenarios must be stored in your resources folder.

## How to write KITE scenario

A KITE scenario is a JSON file containing the following fields :

description - The description of what the test does
dependencies - The ordered list of scenarios to execute prior to the test.
variables - The dictionnary of variables
commands - The ordered list of command to execute during the test
For instance :

{
    "description": "Check that no can access this endpoint without authorizations",
    "variables": {
        "login": "John",
        "badPassword": "1234",
        "goodPassword": "N#2B5&jcP5z5KCvs"
    },
    "dependencies": [
        "createUser.json"
    ],
    "commands": []
}
## Placeholders

To make the development easier and minimal, some placeholder are available and will be replaced at runtime wherever you put it.

Placeholders are scoped the current scenario and the dependent scenarios.

Variables : {{Variable:MyVariableName}} will produce value where value is the value defined in the variable node.
UUID : {{UUID}} will produce a random UUID and associate it with the current object of the POST request.
UUID : {{UUID:User01}} will produce the UUID associated with the object named User01.
URI : {{Location:User01}} will produce the full URI of the object named User01.
For the two lated cases, the object User01 must have been created before referenced.

## Command node

A command corresponds to a HTTP command. It contains the following fields :

description - The description of the command performed
verb - The HTTP verb to use [GET, POST, GET, DELETE, HEAD, PUT]
uri - The URI to perform the HTTP operation against
body - The body of the HTTP operation
headers - The dictionnary of the header values to use
expectedStatus - The expected response status of the command
wait - The number of second to wait before the command
name - Usable during a POST, it allow to name a created resource to use it later

###POST

During a POST, the default expected status is 201.

Sample scenario :

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
### GET

During a GET, the default expectedStatus is 200.

Sample scenario :

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
## Check node

Methods

The following methods are available :

* exists - Return true if the field was found, false otherwise
* length - Return the length of the specified list field
* contains - Return true if all the specified values were found in the specified field.
* nop - Return the field value. This method is the default one and should not be specified when used.
Operators

The following operators are available :

* equals - Return true if the expected value and the actual value are equals, false otherwise. This operator is the default one and should not be specified when used.
* notequals - Return false if the expected value and the actual value are equals, true otherwise.
* gt - Return true if the actual value is greater than the expected, false otherwise.
