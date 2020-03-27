package ch.cern;

import java.util.List;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * Interface as boilerplate for lambda query functions
 */
public interface ZKQuery {
    Boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLs);
}