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
import org.junit.jupiter.api.*;
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

        config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2281", 2000, "GREEN", "RED", "", "");

        zktree = new ZKTree(zkeeper, config);

    }

    @AfterAll
    public void stopZookeeper() throws IOException {
        cli.close();
        zkTestServer.stop();
    }

    /*
     * @Test public void testExportAllTree() throws Exception {
     * Assertions.assertEquals(2115520187, zktree.queryTree("exportAll", "/",
     * null).hashCode()); }
     * 
     * @Test public void testExportAllFind() throws Exception {
     * Assertions.assertEquals(1131947301, zktree.queryFind("exportAll", "/",
     * null).hashCode()); }
     */

    @Test
    public void testNoACLTree() throws Exception {
        Assertions.assertEquals(1230689787, zktree.queryTree("noACL", "/", null).hashCode());
    }

    @Test
    public void testNoACLFind() throws Exception {
        Assertions.assertEquals(-347680801, zktree.queryFind("noACL", "/", null).hashCode());
    }

    /*
     * @Test public void testPartialACLTree() throws Exception {
     * Assertions.assertEquals(1194669308, zktree.queryTree("partialACL", "/", new
     * String[] { "ip:127.0.0.3:rda" }).hashCode()); }
     * 
     * @Test public void testPartialACLFind() throws Exception {
     * Assertions.assertEquals(10729885, this.zktree.queryFind("partialACL", "/",
     * new String[] { "ip:127.0.0.3:rda" }).hashCode()); }
     */
    @Test
    public void testParentYesChildNoTree() throws Exception {
        Assertions.assertEquals(213120819, this.zktree.queryTree("parentYesChildNo", "/", null).hashCode());
    }

    @Test
    public void testParentYesChildNoFind() throws Exception {
        Assertions.assertEquals(-1662130706, this.zktree.queryFind("parentYesChildNo", "/", null).hashCode());
    }

    @Test
    public void testExactACLTree() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        Assertions.assertEquals(874505468, this.zktree
                .queryTree("exactACL", "/", new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" })
                .hashCode());
    }

    @Test
    public void testExactACLFind() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        Assertions.assertEquals(10729885, this.zktree
                .queryFind("exactACL", "/", new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" })
                .hashCode());
    }

}
