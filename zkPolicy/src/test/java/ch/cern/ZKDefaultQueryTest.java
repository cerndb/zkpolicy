package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    ZKConnection zkConnection;

    @BeforeAll
    public void startZookeeper() throws Exception {
        // Choose an available port
        zkTestServer = new TestingServer(2281);
        cli = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
        cli.start();
        zkConnection = new ZKConnection();
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
        // auth scheme ignores the passed id and matches the current authentication
        List<ACL> aclListC = new ArrayList<ACL>();
        aclListC.add(new ACLAugment("auth:lelele:crda").getACL());
        zkeeper.create("/c", "c".getBytes(), aclListC, CreateMode.PERSISTENT);

        List<ACL> aclListCC = new ArrayList<ACL>();
        aclListCC.add(new ACLAugment("world:anyone:crwda").getACL());
        zkeeper.create("/c/cc", "cc".getBytes(), aclListCC, CreateMode.PERSISTENT);

        config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2281", 2000, "GREEN", "RED", "", "");

        zktree = new ZKTree(zkeeper, config);
    }

    @AfterAll
    public void stopZookeeper() throws IOException, InterruptedException {
        cli.close();
        zkTestServer.stop();
        this.zkConnection.close();
    }

    @Test
    public void testNoACLTree() throws Exception {
        assertEquals(1230689787, zktree.queryTree("noACL", "/", null).hashCode());
    }

    @Test
    public void testNoACLFind() throws Exception {
        assertEquals(-347680801, zktree.queryFind("noACL", "/", null).hashCode());
    }

    @Test
    public void testParentYesChildNoTree() throws Exception {
        assertEquals(213120819, this.zktree.queryTree("parentYesChildNo", "/", null).hashCode());
    }

    @Test
    public void testParentYesChildNoFind() throws Exception {
        assertEquals(-1662130706, this.zktree.queryFind("parentYesChildNo", "/", null).hashCode());
    }

    @Test
    public void testExactACLTree() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        assertEquals(874505468, this.zktree
                .queryTree("exactACL", "/", new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" })
                .hashCode());
    }

    @Test
    public void testExactACLFind() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        assertEquals(10729885, this.zktree
                .queryFind("exactACL", "/", new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" })
                .hashCode());
    }

    @Test
    public void testRegexMatchACLTree() throws Exception {
        assertEquals(308385403, this.zktree
                .queryTree("regexMatchACL", "/", new String[] { "digest:.*:.*r.*"})
                .hashCode());
    }

    @Test
    public void testRegexMatchACLFind() throws Exception {
        assertEquals(-1539084560, this.zktree
                .queryFind("regexMatchACL", "/", new String[] { "digest:.*:.*r.*"})
                .hashCode());
    }

    @Test
    public void testGlobMatchACLTree() throws Exception {
        assertEquals(308385403, this.zktree
                .queryTree("globMatchACL", "/", new String[] { "digest:*:*r*"})
                .hashCode());
    }

    @Test
    public void testGlobMatchACLFind() throws Exception {
        assertEquals(-1539084560, this.zktree
                .queryFind("globMatchACL", "/", new String[] { "digest:*:*r*"})
                .hashCode());
    }

}
