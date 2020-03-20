package ch.cern;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeTest {
    TestingServer zkTestServer;
    CuratorFramework cli;

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
    public void testZKTreeQueryTree() throws Exception {
        if (cli.checkExists().forPath("/crud") != null) {
            delete(cli, "/crud");
        }
        create(cli, "/crud/a", "a is a node".getBytes());

        // All nodes satisfy this query
        ZKQuery exportAll = (aclList) -> {
            return true;
        };

        ZKTree zktree = new ZKTree(cli.getZookeeperClient().getZooKeeper());
        Assertions.assertEquals(-196637193, zktree.queryTree("/", exportAll).hashCode());
    }

    @Test
    public void testZKTreeQueryFind() throws Exception {
        if (cli.checkExists().forPath("/crud") != null) {
            delete(cli, "/crud");
        }
        create(cli, "/crud/a", "a is a node".getBytes());
        // All nodes satisfy this query
        ZKQuery exportAll = (aclList) -> {
            return true;
        };

        ZKTree zktree = new ZKTree(cli.getZookeeperClient().getZooKeeper());

        Assertions.assertEquals(-315108666, zktree.queryFind("/", exportAll).hashCode());
    }

    public static void create(CuratorFramework client, String path, byte[] payload) throws Exception {
        // this will create the given ZNode with the given data
        client.create().creatingParentsIfNeeded().forPath(path, payload);
    }

    public static void delete(CuratorFramework client, String path) throws Exception {
        // delete the given node
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }
}
