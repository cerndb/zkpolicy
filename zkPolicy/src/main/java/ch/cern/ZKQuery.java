package ch.cern;

import java.util.List;

import org.apache.zookeeper.data.ACL;

/**
 * Interface as boilerplate for lambda query functions
 */
public interface ZKQuery {
    Boolean query(List<ACL> aclList);
}