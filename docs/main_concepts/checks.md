# Checks

A `check` is confirming compliance with a specific set of ACL entries on znodes with paths matching a regular expression. In the next section, we include examples for check executions.  

## Examples
In this section, we will provide check examples on the following ZNode tree state:

```
/
├─── /hbase-secure
│     ├─── /master
│     ├─── /meta-region-server
│     └─── /hbaseid
├─── /hbase-unsecure
├─── /llap-sasl
│     ├─── /user-user-name1
│     └─── /user-user_name2
└─── /zookeeper
      ├─── /config
      └─── /quota
```

```
/ - world:anyone:cdrwa 
/hbase-secure - sasl:hbase:cdrwa, world:anyone:r
/hbase-secure/master - sasl:hbase:cdrwa, world:anyone:r
/hbase-secure/meta-region-server - sasl:hbase:cdrwa, world:anyone:r
/hbase-secure/hbaseid - sasl:hbase:cdrwa, world:anyone:r
/hbase-unsecure - world:anyone:cdrwa
/llap-sasl - sasl:hive:cdrwa, world:anyone:r
/llap-sasl/user-user_name1 - sasl:hive:cdrwa, world:anyone:r
/llap-sasl/user-user_name2 - sasl:hive:cdrwa, world:anyone:r
/zookeeper - world:anyone:cdrwa
/zookeeper/config - world:anyone:r
/zookeeper/quota - world:anyone:cdrwa
```

### Check 1
Check that all znodes under `/hbase-secure` are open for global read while write protected:

```
$ zkpolicy -c <config_file> check -p /hbase-secure -e .* -a sasl:hbase:cdrwa -a world:anyone:r

Check Result: PASS

/hbase-secure : PASS
/hbase-secure/master : PASS
/hbase-secure/meta-region-server : PASS
/hbase-secure/hbaseid : PASS
```

### Check 2
Check that all znodes `/llap-sasl/user-*` are not open for global read:

```
$ zkpolicy -c <config_file> check -p /llap-sasl -e /llap-sasl/user-.* -a sasl:hive:cdrwa

Check Result: FAIL

/llap-sasl/user-user_name1 : FAIL
/llap-sasl/user-user_name2 : FAIL
```
