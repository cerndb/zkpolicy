# Contributing to zkpolicy

Contributions of any kind are more than welcome, provided that they adhere to the contribution guidelines specified in this document.

Codebase contributions are not the sole form of help in the development of this tool. A contributor can also:
* Report bugs and problems or suggest features using the Issue tracker of this repository
* Implement tests covering additional execution scenarios and code segments
* Support the development in any other logical way.

## First steps

### Search issues; create an issue if necessary

Is there already an issue that addresses your concern? Search in our issue tracker to see if you can find something similar. If you do not find something similar, please create a new issue before submitting a pull request unless the change is truly trivial -- for example: typo fixes, removing compiler warnings, etc.

## Fork and clone the repository

After assuring that your concern is valid, you should fork the main zkpolicy repository and clone it to your local development host.

## Create a Branch

### Branch from qa
Please submit all pull requests to qa, even bug fixes and minor improvements.

### Branch naming conventions

```
<group>/<branch-name>
```

* Use grouping tokens (words) at the beginning of your branch names.
    ```
    wip       Work in progress;
    feat      Feature addition or expansion
    bug       Bug fix
    doc       Documentation of any kind
    test      Tests of any kind
    refactor  Code refactoring
    ```
* Define and use short lead tokens to differentiate branches in a way that is meaningful to your workflow.
* Use slashes to separate parts of your branch names.
* Do not use bare numbers as leading parts.
* Branches should preferably be named using succinct, lower-case, underscore (_) delimited names, such as `main_concepts`, `check_cli`, etc.

Some valid branch names are: `doc/usage`, `feat/tree_cli` ...

## Follow zkpolicy coding style

zkpolicy follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

## Add license header

Every zkpolicy source code file should begin with the following license preamble:
```
Copyright © 2020, CERN
This software is distributed under the terms of the MIT Licence,
copied verbatim in the file 'LICENSE'. In applying this licence,
CERN does not waive the privileges and immunities
granted to it by virtue of its status as an Intergovernmental Organization
or submit itself to any jurisdiction.
```

## Prepare your commit

### Submit JUnit test cases for all behaviour changes

Any behaviour change should be accompanied with JUnit test cases. For zkpolicy, those are located in `src/test/java/ch/cern`. Each class should have its own tests on that directory, named after her name with the suffix `Test`. So for class `Class1` the accepted test class name is `Class1Test`.

If those tests involve a ZooKeeper instance, please use [Apache Curator](https://curator.apache.org/) for firing up a TestingInstance. An example of curator usage for zkpolicy can be found at the `startZookeeper()` method of `src/test/java/ch/cern`.

## Run the Final Checklist

### Run all tests prior to submission

See the `Building from source` and `Testing` section of the README for instructions. Make sure that all tests pass prior to submitting your pull request.

### Submit your pull request

Be sure to:
* Explain your use case. What led you to submit this change? Why were existing mechanisms in the framework insufficient? Make a case that this is a general-purpose problem and that yours is a general-purpose solution, etc.
* Add any additional information and ask questions; start a conversation or continue one from an issue.
* Mention the issue ID.

### Mention your pull request on the associated issue

Add a comment to the associated issue(s) linking to your new pull request.
