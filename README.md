# ZooKeeper Policy Audit Tool

### Table of contents
1. [Overview](#overview)
2. [Documentation](#documentation)
3. [Contribution](#contribution)
4. [Installation](#installation)
    * [Requirements](#requirements)
    * [Using the RPM package](#using-the-rpm-package)
    * [Building from source](#building-from-source)
5. [Use as Maven dependency](#use-as-maven-dependency)
6. [Using the tool](#using-the-tool)
7. [Configuration](#configuration)

## Overview
Zookeeper Policy Audit Tool (aka zkPolicy) for checking and enforcing ACLs on ZNodes.

This repository contains all the source code for the tool, that uses the ZooKeeper Java API, as well as proposed default configuration for auditing and enforcing policies on the ZooKeeper ZNode tree.

## Documentation
See the current [reference docs](docs/README.md).

## Contribution
Contributions of any form are welcome, provided that they adhere to the [contributor guidelines](CONTRIBUTING.md).

## Installation

### Requirements
* ZooKeeper >= 3.4.13
* Java SDK >= 1.8

### Using the RPM package
zkpolicy is packaged in RPM and can be installed using:
```
yum install cerndb-sw-zkpolicy
```

### Building from source
###### Requirements
* Maven >= 3.6

The project is built using `maven`. In order to build the project:

1. Clone this repository
2. ```bash
   cd zookeeper-policy-audit-tool/zkPolicy
   mvn package -DskipTests
   ```

This command generates the `.jar` artifact at the `zkPolicy/target/` directory.

To test zkpolicy:

```bash
mvn test
```

## Use as code dependency

In order to use zkpolicy as a Java Dependency, consult the corresponding [Sonatype index page](https://search.maven.org/artifact/ch.cern/cerndb-sw-zkpolicy)
for different dependency managers (e.g. Maven, Gradle). Indicatively:

### Maven

```xml
<dependency>
  <groupId>ch.cern</groupId>
  <artifactId>cerndb-sw-zkpolicy</artifactId>
  <version>1.0.1-13</version>
</dependency>
```

### Gradle

```
implementation 'ch.cern:cerndb-sw-zkpolicy:1.0.1-13'
```

## Using the tool
The tool can be executed either by the `.jar` file:

```bash
java -jar ./target/zkpolicy-<Version>.jar [arguments]
```

or using the alias created by the tool package:

```bash
zkpolicy [arguments]
```

For usage details, please consult the tool man pages or the [tool documentation](docs/README.md#usage).

## Configuration
The tool execution is configured by the main configuration file that is passed as a parameter to the `-c, --config` flag. More information about the structure of the configuration file can be found at the [tool documentation](docs/README.md#configuration).