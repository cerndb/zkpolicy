package ch.cern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeNodeTest {
  TestingServer zkTestServer;
  CuratorFramework cli;

  @TempDir
  File testTempDir;

  @Test
  public void testZKTreeNodeConstructor() throws Exception {
    List<ACL> aclList = new ArrayList<ACL>();
    aclList.add(new ACL(30, new Id("world", "anyone")));
    aclList.add(new ACL(30, new Id("digest", "testuser:testpass")));

    ZKTreeNode child1 = new ZKTreeNode("/path/child1", "child1 data".getBytes(), null, null, null);
    ZKTreeNode child2 = new ZKTreeNode("/path/child2", "child2 data".getBytes(), null, null, null);
    ZKTreeNode[] children = { child1, child2 };

    ZKTreeNode zkTreeNode = new ZKTreeNode("/path", "test data".getBytes(), aclList, children, null);
    assertAll(() -> assertEquals("/path", zkTreeNode.getPath()),
        () -> assertArrayEquals("test data".getBytes(), zkTreeNode.getData()),
        () -> assertArrayEquals(children, zkTreeNode.getChildren()),
        () -> assertArrayEquals(aclList.toArray(), zkTreeNode.getAcl().toArray()));
  }
}
