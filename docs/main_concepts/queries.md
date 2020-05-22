# Queries

One of the main concepts of ZooKeeper Policy Auditing Tool is the `query`. Queries provide the znode paths that satisfy a certain requirement. The tool implements the following default queries:

1. **duplicateACL**: Return znode paths that have duplicate ACL entries. This issue is fixed in current ZooKeeper versions but it is still a problem for older versions as well as latest ones that where updated from snapshots that allowed duplicate entries.
2. **exactACL**: Return znode paths with ACL definition exactly matching the one queried by the user.
3. **globMatchACL**: Return znode paths with ACL entries that match the glob pattern query argument.
4. **globMatchPath**: Return znode paths that match the glob pattern query argument.
5. **noACL**: Return znode paths that have no ACL restrictions (so their ACL includes only the `world:anyone:cdrwa` entry).
6. **parentYesChildNo**: Return znode paths with ACL definition not complying to their parent ACL.
7. **regexMatchACL**: Return znode paths with ACL entries that match the regular expression pattern query argument.
8. **regexMatchPath**: Return znode paths that match the regular expression query argument.
9. **satisfyACL**: Return znode paths with ACL definition satisfying the one queried by the user.

## Examples
In this section, we will provide example results for each one of the queries on the following ZNode tree state:

```
/
├─── /a
│     └─── /aa
├─── /b
├─── /c
│     └─── /cc
├─── /d
└─── /zookeeper
      ├─── /config
      └─── /quota
```

```
/ - world:anyone:cdrwa 
/a - digest:user1:passw1:cdrwa
/a/aa - digest:user1:passw1:cdrwa
/b - digest:user2:passw2:cdrwa, ip:127.0.0.3:rda
/b/bb - ip:127.0.0.3:rda
/c - auth:lelele:crda
/c/cc - world:anyone:cdrwa
/d - world:anyone:cdrwa, world:anyone:cdrwa
/zookeeper - world:anyone:cdrwa
/zookeeper/config - world:anyone:r
/zookeeper/quota - world:anyone:cdrwa
```
The queries will be executed recursively from the root path `/` and from a user that cannot satisfy ip:127.0.0.3:* ACL entries, but can satisfy every `auth` and `digest` scheme ACL entries.

### duplicateACL
```
$ zkpolicy -c <config_file> query duplicateACL -p /

WARNING: No READ permission for /b/bb, skipping subtree
/d
``` 

Notice that when the user executing the query has no READ permission for a certain subtree (so fetching of children paths and ACl is not possible), a `WARNING` message is shown.

### exactACL
```
$ zkpolicy -c <config_file> query exactACL -p / -a digest:user2:[passw2 digest]:cdrwa -a ip:127.0.0.3:rda

/b
WARNING: No READ permission for /b/bb, skipping subtree
``` 

### globMatchACL
In this example, we query for znodes that have at least one ACL entry using the digest schema that allows write permission for data.
```
$ zkpolicy -c <config_file> query globMatchACL -p / -a digest:*:*w*

/a
/a/aa
/b
WARNING: No READ permission for /b/bb, skipping subtree
``` 

### globMatchPath
```
$ zkpolicy -c <config_file> query globMatchPath -p / -a /*/*

/a
/a/aa
/b
WARNING: No READ permission for /b/bb, skipping subtree
```

### noACL
```
$ zkpolicy -c <config_file> query noACL -p / 

/
WARNING: No READ permission for /b/bb, skipping subtree
/c/cc
/d
/zookeeper
/zookeeper/config
/zookeeper/quota
```

### parentYesChildNo
```
$ zkpolicy -c <config_file> query parentYesChildNo -p / 

/a
/b
WARNING: No READ permission for /b/bb, skipping subtree
/c
/c/cc
/zookeeper/config
```

### regexMatchACL
```
$ zkpolicy -c <config_file> query regexMatchACL -p / -a digest:.*:.*w.*

/a
/a/aa
/b
WARNING: No READ permission for /b/bb, skipping subtree
```

### regexMatchPath
```
$ zkpolicy -c <config_file> query regexMatchPath -p / -a /.*/.*

/a
/a/aa
/b
WARNING: No READ permission for /b/bb, skipping subtree
```

### satisfyACL
```
$ zkpolicy -c <config_file> query satisfyACL -p / -a digest:user1:[passw1 digest]:r

/
/a
/a/aa
WARNING: No READ permission for /b/bb, skipping subtree
/c/cc
/d
/zookeeper
/zookeeper/config
/zookeeper/quota
```



