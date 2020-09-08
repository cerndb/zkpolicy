# Main concepts

## ZooKeeper concepts
For information regarding ZooKeeper concepts, please consult the [Administrator's Guide](https://zookeeper.apache.org/doc/r3.6.1/zookeeperAdmin.html).

## ZooKeeper Policy Audit Tool concepts
The tool is introducing a number of elements that are essential for interacting with the ZNode tree. 

### Query
A query is executed upon a specific subtree of the ZNode tree to answer the following question:
> Which ZNodes satisfy a specific ACL requirement?

In order to execute a query using the CLI, the following command is needed:
```bash
zkpolicy -c <config_file.yml> query [QUERY_NAME] -p [ROOT_PATH] [QUERY ARGS]
```

The tool defines numerous default queries for commonly requested scenarios. More info on the available queries can be found [here](./queries.md).

### Check
A check is executed upon a specific set of znodes that are defined either explicitly or using regular expressions for path matching. It is used to answer the following question:
> Do specific ZNodes satisfy a specific ACL requirement?

In order to execute a check using the CLI, the following command is needed:
```bash
zkpolicy -c <config_file.yml> check -e [PATH PATTERN] -p [ROOT_PATH] -a [CHECK ACLS]
```

More info as well as examples of check execution can be found [here](./checks.md).

### Policy
A policy is a structure consisting of a set of ACL elements that should be applied in a specific set of nodes. This set of nodes can either be explicitly set for each policy or created on policy enforcing using one of the defined queries. A policy is equivalent to the following action:

> Apply a set of ACL elements to all znodes that satisfy a specific requirement.

It is clear that each policy is related to a specific query on the ZNode tree. Policies are enforced using the `enforce` sub-command. 
