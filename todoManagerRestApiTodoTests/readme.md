To run the code, start the API server first, then run the tests with Maven.

1. Start the server:
   java -jar runTodoManagerRestAPI-1.5.5.jar

2. Run the tests:
   cd todoManagerRestApiTodoTests
   mvn clean test

The tests will connect to localhost:4567. If the server is not running, the Environment class will attempt to auto-start the JAR.
