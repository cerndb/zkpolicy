package ch.cern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKDefaultQueryTest {
    TestingServer zkTestServer;
    CuratorFramework cli;
    ZooKeeper zkeeper;
    ZKConfig config;
    ZKTree zktree;

    @BeforeAll
    public void startZookeeper() throws Exception {
        // Choose an available port
        zkTestServer = new TestingServer(2281);
        cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
        cli.start();
        ZKConnection zkConnection = new ZKConnection();
        zkeeper = zkConnection.connect("127.0.0.1:2281", 2000);

        // Setup the znode tree for tests
        // a subtree
        List<ACL> aclListA = new ArrayList<ACL>();
        String tempDigest = DigestAuthenticationProvider.generateDigest("user1:passw1");
        aclListA.add(new ACLAugment("digest:" + tempDigest + ":crwda").getACL());
        zkeeper.create("/a", "a".getBytes(), aclListA, CreateMode.PERSISTENT);

        zkeeper.addAuthInfo("digest", "user1:passw1".getBytes());
        zkeeper.create("/a/aa", "aa".getBytes(), aclListA, CreateMode.PERSISTENT);

        // b subtree
        List<ACL> aclListB = new ArrayList<ACL>();
        tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        aclListB.add(new ACLAugment("digest:" + tempDigest + ":crda").getACL());
        aclListB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());

        zkeeper.addAuthInfo("digest", "user2:passw2".getBytes());

        zkeeper.create("/b", "b".getBytes(), aclListB, CreateMode.PERSISTENT);

        List<ACL> aclListBB = new ArrayList<ACL>();
        aclListBB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());
        zkeeper.create("/b/bb", "bb".getBytes(), aclListBB, CreateMode.PERSISTENT);

        // c subtree
        List<ACL> aclListC = new ArrayList<ACL>();
        aclListC.add(new ACLAugment("auth:user3:pass3:crda").getACL());
        zkeeper.create("/c", "c".getBytes(), aclListC, CreateMode.PERSISTENT);

        List<ACL> aclListCC = new ArrayList<ACL>();
        aclListCC.add(new ACLAugment("world:anyone:crwda").getACL());
        zkeeper.create("/c/cc", "cc".getBytes(), aclListCC, CreateMode.PERSISTENT);

        config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2281", 2000, "green", "\u001B[32m", "red",
                "\u001B[31m");

        zktree = new ZKTree(zkeeper, config);

    }

    @AfterAll
    public void stopZookeeper() throws IOException {
        cli.close();
        zkTestServer.stop();
    }

    @Test
    public void testExportAllTree() throws Exception {
        String[] optionArgs = { "exportAll" };
        Assertions.assertEquals(679433963, zktree.queryTree("/", optionArgs).hashCode());
    }

    @Test
    public void testExportAllFind() throws Exception {
        String[] optionArgs = { "exportAll" };
        Assertions.assertEquals(1131947301, zktree.queryFind("/", optionArgs).hashCode());
    }

    @Test
    public void testNoACLTree() throws Exception {
        String[] optionArgs = { "noACL" };
        Assertions.assertEquals(1868564105, zktree.queryTree("/", optionArgs).hashCode());
    }

    @Test
    public void testNoACLFind() throws Exception {
        String[] optionArgs = { "noACL" };
        Assertions.assertEquals(-347680801, zktree.queryFind("/", optionArgs).hashCode());
    }

    @Test
    public void testPartialACLTree() throws Exception {
        String[] optionArgs = { "partialACL", "ip:127.0.0.3:rda" };
        Assertions.assertEquals(1247125164, zktree.queryTree("/", optionArgs).hashCode());
    }

    @Test
    public void testPartialACLFind() throws Exception {
        String[] optionArgs = { "partialACL", "ip:127.0.0.3:rda" };
        Assertions.assertEquals(10729885, this.zktree.queryFind("/", optionArgs).hashCode());
    }

    @Test
    public void testParentYesChildNoTree() throws Exception {
        String[] optionArgs = { "parentYesChildNo" };
        Assertions.assertEquals(1776009771, this.zktree.queryTree("/", optionArgs).hashCode());
    }

    @Test
    public void testParentYesChildNoFind() throws Exception {
        String[] optionArgs = { "parentYesChildNo" };
        Assertions.assertEquals(-1662130706, this.zktree.queryFind("/", optionArgs).hashCode());
    }

    @Test
    public void testExactACLTree() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        String[] optionArgs = { "exactACL", "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" };
        Assertions.assertEquals(1247125164, this.zktree.queryTree("/", optionArgs).hashCode());
    }

    @Test
    public void testExactACLFind() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        String[] optionArgs = { "exactACL", "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" };
        Assertions.assertEquals(10729885, this.zktree.queryFind("/", optionArgs).hashCode());
    }

}
