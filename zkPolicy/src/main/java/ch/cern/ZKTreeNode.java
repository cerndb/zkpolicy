package ch.cern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import lombok.AccessLevel;

/**
 * Object that represents a ZooKeeper znode.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKTreeNode {

  private String path;
  private byte[] data;
  private List<ACL> acl;
  private ZKTreeNode[] children;
  private Stat stat;
}