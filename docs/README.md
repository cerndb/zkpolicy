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

##### Enforcing service policy
In order to enforce a policy for a single service, use the `--service-policy, -s` option as follows:

```bash
zkpolicy -c config.yml enforce -s <service_name>
```

Multiple service policies can be enforced by appending `-s <service_name>` to the previous command:

```bash
zkpolicy -c config.yml enforce -s <service_name_1> -s <service_name_2>
```

zkpolicy ships with default policy definitions for numerous services (e.g. Kafka, Hbase, HDFS). Those can be found [here](../configs/default/policies/).

##### Dry run execution
Enforcing policies is a nonreversible operation so an admin has to be sure about the changes to be imposed. For this reason, both execution ways of enforcing can be executed in a `dry-run` mode. For `dry-run`, please append `-d` to the previous commands.

#### Rollback enforced policies
In case of incorrect enforcing, rollback functionality is provided by zkpolicy. Before enforcing a policy, zkpolicy creates by a rollback state file (by default `/opt/zkpolicy/rollback/ROLLBACK_STATE_<DATETIME>.yml`). The rollback state path can be changed using the `--rollback-export` option of the `enforce` subcommand.

In order to rollback to a pre enforce state, use the `rollback` subcommand:

```bash
zkpolicy -c config.yml rollback -i /opt/zkpolicy/rollback/ROLLBACK_STATE_<DATETIME>.yml
```

**Warning:** It is advised to execute `rollback` only when authenticated as superuser to the ZooKeeper server or being sure that you have read and admin permissions for all nodes that where affected by the previous `enforce` operation.

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
```

An example configuration file can be found [here](../configs/examples/config_example.yml).

