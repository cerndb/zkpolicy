# zkPolicy Documentation

Welcome to zkPolicy documentation!

### Table of contents
1. [Features](#features)
2. [Docs](#docs)
    * [Getting Started](#getting-started)
        * [Installation](#installation)
            * [Requirements](#requirements)
            * [Using the RPM package](#using-the-rpm-package)
            * [Building from source](#building-from-source)
        * [Java docs generation](#java-docs-generation)
        * [Manpages generation](#manpages-generation)
3. [Main Concepts](#main-concepts)
4. [User Guide](#user-guide)
    * [Usage](#usage)
        * [Auditing](#auditing)
        * [Enforcing](#enforcing)
        * [Visualization](#visualization)
        * [Exporting](#exporting)
    * [Configuration](#configuration)

## Features
zkPolicy is a ZooKeeper tool that provides:
* Visualization of ZooKeeper zNode tree structure
* Generation of reports for ZooKeeper security auditing (with sections for Four Letter Words, ACL overview for tree, query and check results)
* Enforcing ACL policies on znode subtrees
* Querying nodes with specific ACL definitions
* Check nodes for ACL policy satisfaction
* Export tree structure in file format
* CLI or execution using configuration files

## Docs
### Getting Started
#### Installation
##### Requirements
* ZooKeeper >= 3.4.13
* Java SDK >= 1.8

##### Using the RPM package
zkpolicy is packaged in RPM and can be installed using:
```
yum install cerndb-sw-zkpolicy
```

##### Building from source
###### Requirements
* Maven >= 3.6

The project is built using `maven`. In order to build the project:

1. Clone this repository
2. ```bash
   cd zookeeper-policy-audit-tool/zkPolicy
   mvn package -DskipTests
   ```

This commands generates the wanted `.jar` in the `zkPolicy/target/` directory.

#### Java docs generation
This project uses `javadoc` for documentation of the API developed. In order to generate the complete documentation, one has to execute the following command:

```bash
mvn javadoc:javadoc
```

#### Manpages generation
Using picocli [auto usage documentation generation](https://picocli.info/#_generate_man_page_documentation), manpages are generated on project build and are located at `target/generated-docs`.

## Main concepts
zkPolicy introduces several concepts as described [here](main_concepts/README.md).

## User Guide
### Usage
In this section we are going to outline the usage of zkPolicy for the main functionality of the tool. For complete information and usage guides for zkPolicy, please consult the tool man pages.

#### Auditing
Using the zkPolicy auditing functionality, a user can generate useful reports for the ACLs defined throughout a ZooKeeper instance. Those reports include the results for queries and checks defined at the audit configuration file as well as additional information regarding the ZooKeeper Server (List of ACL definitions for every znode accessible by the tool user, Four Letter Words information and ZooKeeper instance connection details).

```bash
zkpolicy -c config.yml audit -i [audit_config.yml]
```

An example audit configuration file can be found [here](../configs/examples/audit_example.yml).

#### Enforcing
zkPolicy offers policy enforcing functionality. Using its CLI, a user can either enforce a single policy or a series of policies defined in a policies configuration file.

##### Enforce a single policy
Enforce a specific set of ACL elements on every znode matching a specific query:
```bash
zkpolicy -c config.yml enforce -P acl_1 -P acl_2... noACL -p /
```

With the previous command the ACL `[acl_1,acl_2]` is applied recursively to all nodes satisfying the `noACL` query, starting from the zNode tree root.

##### Enforcing multiple policies
There are many cases when it is useful for an admin to enforce a series of policies. For this scenario, zkPolicy provides enforcing through a configuration file functionality:

```bash
zkpolicy -c config.yml enforce -i policies_config.yml
```
An example policies configuration file can be found [here](../configs/examples/policies_example.yml).

##### Dry run execution
Enforcing policies is a nonreversible operation so an admin has to be sure about the changes to be imposed. For this reason, both execution ways of enforcing can be executed in a `dry-run` mode. For `dry-run`, please append `-d` to the previous commands.

#### Visualization
To get an visual representation of the ZooKeeper tree structure, zkPolicy offers the `tree` subcommand:

```bash
zkpolicy -c config.yml tree -p /root
```
#### Exporting
zkPolicy offers ZooKeeper tree exporting functionality in human readable as well as compact form in JSON and YAML formats:

```bash
zkpolicy -c config.yml export -p /root -t json -o exported_output.json
```

For compact output, append `-C` to the previous command.

### Configuration
The tool execution is configured by the main configuration file that is passed as a parameter to the `-c, --config` flag. This is a `.yml` file of the following format:

```yml
---
timeout: 2000
zkServers: "127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181"

# colors for tree view output
# Available colors: RED, GREEN, BLACK, BLUE, YELLOW, MAGENTA, CYAN, WHITE
matchColor: "GREEN"
mismatchColor: "RED"
jaas: "/path/to/jaas.conf"

# This path will be used when the audit subcommand is executed without any further arguments.
defaultAuditPath: "/path/to/defaultAudit.yml"
```

An example configuration file can be found [here](../configs/examples/config_example.yml).

