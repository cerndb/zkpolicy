package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKEnforceTest {
    TestingServer zkTestServer;
    CuratorFramework cli;
    ZooKeeper zkeeper;
    ZKConfig config;
    ZKEnforce zkEnforce;
    ZKConnection zkConnection;
    private final PrintStream originalStdOut = System.out;
    private ByteArrayOutputStream consoleContent = new ByteArrayOutputStream();

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

        List<ACL> aclListABB = new ArrayList<ACL>();
        aclListABB.add(new ACLAugment("digest:testuser:testpass:crwda").getACL());

        zkeeper.create("/a/bb", "aa".getBytes(), aclListABB, CreateMode.PERSISTENT);
        // b subtree
        List<ACL> aclListB = new ArrayList<ACL>();
        tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        aclListB.add(new ACLAugment("digest:" + tempDigest + ":crda").getACL());
        aclListB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());

        zkeeper.addAuthInfo("digest", "user2:passw2".getBytes());

        zkeeper.create("/b", "b".getBytes(), aclListB, CreateMode.PERSISTENT);

        List<ACL> aclListBB = new ArrayList<ACL>();
        aclListBB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());
        aclListBB.add(new ACLAugment("world:anyone:ra").getACL());
        zkeeper.create("/b/bb", "bb".getBytes(), aclListBB, CreateMode.PERSISTENT);

        // c subtree
        List<ACL> aclListC = new ArrayList<ACL>();
        aclListC.add(new ACLAugment("auth:user3:pass3:crda").getACL());
        zkeeper.create("/c", "c".getBytes(), aclListC, CreateMode.PERSISTENT);

        List<ACL> aclListCC = new ArrayList<ACL>();
        aclListCC.add(new ACLAugment("world:anyone:crwda").getACL());
        zkeeper.create("/c/cc", "cc".getBytes(), aclListCC, CreateMode.PERSISTENT);

        config = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2281", 2000, "GREEN", "RED", "", "");
        zkEnforce = new ZKEnforce(zkeeper);
    }

    @AfterAll
    public void stopZookeeper() throws IOException, InterruptedException {
        cli.close();
        zkTestServer.stop();
        zkConnection.close();
    }

    @BeforeEach
    public void beforeTest() {
        // Redirect all System.out to consoleContent.
        System.setOut(new PrintStream(this.consoleContent));
    }

    @AfterEach
    public void afterTest() {
        // Put back the standard out.
        System.setOut(this.originalStdOut);

        // Clear the consoleContent.
        this.consoleContent = new ByteArrayOutputStream();
    }

    @Test
    public void testEnforceDry()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, KeeperException, InterruptedException, NoSuchFieldException {
        this.zkEnforce.enforceDry("satisfyACL", "/", new String[] { "ip:127.0.0.3:r" });
        String expectedResult = "/\n" + "WARNING: No READ permission for /a/bb, skipping subtree\n" + "/b\n"
                + "/b/bb\n" + "/c/cc\n" + "/zookeeper\n" + "/zookeeper/quota\n";
        assertEquals(expectedResult, this.consoleContent.toString());
    }

    @Test
    public void testEnforce() throws NoSuchMethodException, KeeperException, InterruptedException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        String[] policies = { "world:anyone:ra", "ip:127.0.0.4:cda" };
        this.zkEnforce.enforce(policies, "regexMatchACL", "/b", new String[] { "ip:127.0.0.3:.*", "world:anyone:.*" },
                false);
        List<ACL> alteredList = this.zkeeper.getACL("/b/bb", null);
        List<ACL> expectedList = new ArrayList<ACL>();
        expectedList.add(new ACLAugment("world:anyone:ra").getACL());
        expectedList.add(new ACLAugment("ip:127.0.0.4:cda").getACL());
        assertEquals(expectedList, alteredList);
    }

    @Test
    public void testEnforceAppend()
            throws NoSuchMethodException, KeeperException, InterruptedException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        String[] policies = { "ip:127.0.0.3:cr" };
        this.zkEnforce.enforce(policies, "regexMatchACL", "/", new String[] { "ip:127.0.0.4:.*", "world:anyone:.*" },
                true);
        List<ACL> alteredList = this.zkeeper.getACL("/b/bb", null);
        List<ACL> expectedList = new ArrayList<ACL>();
        expectedList.add(new ACLAugment("world:anyone:ra").getACL());
        expectedList.add(new ACLAugment("ip:127.0.0.4:cda").getACL());
        expectedList.add(new ACLAugment("ip:127.0.0.3:cr").getACL());
        assertEquals(expectedList, alteredList);
    }

}
