package ch.cern;

import java.io.IOException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.*;
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

  @BeforeAll
  public void startZookeeper() throws Exception {
    // Choose an available port
    zkTestServer = new TestingServer(2281);
    cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
    cli.start();
  }

  @AfterAll
  public void stopZookeeper() throws IOException {
    cli.close();
    zkTestServer.stop();
  }

  @Test
  public void testZKTreeQueryTreePreOrder() throws Exception {
    if (cli.checkExists().forPath("/crud") != null) {
      delete(cli, "/crud");
    }
    create(cli, "/crud/a", "a is a node".getBytes());

    ZKConfig config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, "green", "\u001B[32m",
        "red", "\u001B[31m");

    ZKTree zktree = new ZKTree(cli.getZookeeperClient().getZooKeeper(), config);
    String[] optionArgs = {"exportAll"};
    Assertions.assertEquals(1168456046, zktree.queryTree("/", optionArgs).hashCode());
  }

  @Test
  public void testZKTreeQueryFind() throws Exception {
    if (cli.checkExists().forPath("/crud") != null) {
      delete(cli, "/crud");
    }
    create(cli, "/crud/a", "a is a node".getBytes());

    ZKConfig config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, "green", "\u001B[32m",
        "red", "\u001B[31m");

    ZKTree zktree = new ZKTree(cli.getZookeeperClient().getZooKeeper(), config);
    String[] optionArgs = {"exportAll"};
    Assertions.assertEquals(-315108666, zktree.queryFind("/", optionArgs).hashCode());
  }
}
