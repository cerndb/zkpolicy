# Zookeeper Policy Audit Tool

Zookeeper audit and policy tool for checking and enforcing ACLs on the znodes.

## Build from source
To build the zkPolicy tool, the following steps are required:

1. Clone this repository
2. While in the zkPOlicy directory, build using maven:
    ```bash
    mvn clean package
    ```
    The build will also execute all implemented tests while packaging.
3. The artifact produced after packaging is located at the `zkPolicy/target/zkpolicy-<version_id>.jar`

## Execute the tool
Using the tool can either be done right from the `.jar` file or by using the respective maven plugin:
1. Use maven to run the project with CLI arguments:
    ```bash
    mvn exec:java -Dexec.args="-h"
    ```
## Documentation
This project uses `javadoc` for documentation of the API developed. In order to generate the complete documentation, one has to execute the following command:

```bash
mvn javadoc:javadoc
```

## Testing
Although the tests are executed along with the execution and packaging procedures, they can also be explicitly executed with the following command:
```bash
mvn clean test
```

For automatic code coverage reports, the `Jacoco` tool is used.