package ch.cern;

import java.util.List;
import org.apache.zookeeper.data.ACL;

/**
 * Interface for queriy functions.
 */
public interface ZKQuery {
    boolean query(List<ACL> aclList, String path, ZKClient zk, String[] queryOptions);
}