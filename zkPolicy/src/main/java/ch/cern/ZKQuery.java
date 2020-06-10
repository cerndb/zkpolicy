package ch.cern;

import java.util.List;
import org.apache.zookeeper.data.ACL;

/**
 * Interface for query functions.
 */
public interface ZKQuery {
  String getDescription();
  boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryOptions);
}