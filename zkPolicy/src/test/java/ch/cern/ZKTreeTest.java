package ch.cern;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeTest {
  TestingServer zkTestServer;
  CuratorFramework cli;

  public static void create(CuratorFramework client, String path, byte[] payload) throws Exception {
    // this will create the given ZNode with the given data
    client.create().creatingParentsIfNeeded().forPath(path, payload);
  }

  public static void delete(CuratorFramework client, String path) throws Exception {
    // delete the given node
    client.delete().deletingChildrenIfNeeded().forPath(path);
  }

}
