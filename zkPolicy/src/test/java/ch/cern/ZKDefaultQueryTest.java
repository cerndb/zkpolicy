package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
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
    ZKConfig config;
    ZKTree zktree;
    ZKClient zkClient;
    String green;
    String red;
    String reset;
    ZKDefaultQuery zkDefaultQuery;

    @BeforeAll
    public void startZookeeper() throws Exception {
        // Choose an available port
        zkTestServer = new TestingServer();
        config = new ZKConfig(zkTestServer.getConnectString(), 2000, "GREEN", "RED", "", "", "");
        this.zkClient = new ZKClient(config);
        
        // Setup the znode tree for tests
        // a subtree
        List<ACL> aclListA = new ArrayList<ACL>();
        String tempDigest = DigestAuthenticationProvider.generateDigest("user1:passw1");
        aclListA.add(new ACLAugment("digest:" + tempDigest + ":crwda").getACL());
        zkClient.create("/a", "a".getBytes(), aclListA, CreateMode.PERSISTENT);

        zkClient.addAuthInfo("digest", "user1:passw1".getBytes());
        zkClient.create("/a/aa", "aa".getBytes(), aclListA, CreateMode.PERSISTENT);

        // b subtree
        List<ACL> aclListB = new ArrayList<ACL>();
        tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        aclListB.add(new ACLAugment("digest:" + tempDigest + ":crda").getACL());
        aclListB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());

        zkClient.addAuthInfo("digest", "user2:passw2".getBytes());

        zkClient.create("/b", "b".getBytes(), aclListB, CreateMode.PERSISTENT);

        List<ACL> aclListBB = new ArrayList<ACL>();
        aclListBB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());
        zkClient.create("/b/bb", "bb".getBytes(), aclListBB, CreateMode.PERSISTENT);

        // c subtree
        // auth scheme ignores the passed id and matches the current authentication
        List<ACL> aclListC = new ArrayList<ACL>();
        aclListC.add(new ACLAugment("auth:lelele:crda").getACL());
        zkClient.create("/c", "c".getBytes(), aclListC, CreateMode.PERSISTENT);

        List<ACL> aclListCC = new ArrayList<ACL>();
        aclListCC.add(new ACLAugment("world:anyone:crwda").getACL());
        zkClient.create("/c/cc", "cc".getBytes(), aclListCC, CreateMode.PERSISTENT);

        zktree = new ZKTree(zkClient);

        this.green = ZKPolicyDefs.Colors.GREEN.getANSIValue();
        this.red = ZKPolicyDefs.Colors.RED.getANSIValue();
        this.reset = ZKPolicyDefs.Colors.RESET.getANSIValue();
        zkDefaultQuery = new ZKDefaultQuery();
    }

    @AfterAll
    public void stopZookeeper() throws IOException, InterruptedException {
        this.zkClient.close();
    }

    @Test
    public void testNoACLTree() throws Exception {

        ZKQuery query = zkDefaultQuery.getValueOf("noACL");

        ZKQueryElement queryElement = new ZKQueryElement("noACL", "/", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutput = String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n";

        String expectedOutput = 
        this.green + "/"+this.reset+"\n" +
        "├─── "+ this.red+"/a"+this.reset+"\n" +
        "│     └─── "+this.red+"/aa" + this.reset + "\n" +
        "├─── "+this.red+"/b"+this.reset+"\n" +
        "├─── "+this.red+"/c" +this.reset+"\n" +
        "│     └─── "+this.green+"/cc" +this.reset+"\n" +
        "└─── "+this.green+"/zookeeper"+this.reset+"\n" +
        "      ├─── "+this.red+"/config"+this.reset+"\n" +
        "      └─── "+this.green+"/quota"+this.reset+"\n"; 
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testNoACLFind() throws Exception {
        ZKQuery query = zkDefaultQuery.getValueOf("noACL");

        ZKQueryElement queryElement = new ZKQueryElement("noACL", "/", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());
        String expectedString = "/\n" + "WARNING: No READ permission for /b/bb, skipping subtree\n" + "/c/cc\n" + "/zookeeper\n" + "/zookeeper/quota";
        
        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        this.zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutString = String.join("\n", queriesOutput.get(queryElement.hashCode()));
        assertEquals(expectedString, actualOutString);
    }

    @Test
    public void testParentYesChildNoTree() throws Exception {
        ZKQuery query = zkDefaultQuery.getValueOf("parentYesChildNo");

        ZKQueryElement queryElement = new ZKQueryElement("parentYesChildNo", "/", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutput = String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n";

        String expectedOutput = 
        this.reset +"/\n" +
        this.red +"├─── "+ this.reset +"/a\n" +
        this.red +"│     "+ this.green +"└─── "+ this.reset +"/aa\n" +
        this.red +"├─── "+ this.reset +"/b\n" +
        this.red +"├─── "+ this.reset +"/c\n" +
        this.red +"│     "+ this.red +"└─── "+ this.reset +"/cc\n" +
        this.green +"└─── "+ this.reset +"/zookeeper\n" +
        this.green +"      "+ this.red +"├─── " + this.reset +"/config\n"+
        this.green +"      "+ this.green +"└─── " + this.reset +"/quota\n";
        
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testParentYesChildNoFind() throws Exception {
        ZKQuery query = zkDefaultQuery.getValueOf("parentYesChildNo");

        ZKQueryElement queryElement = new ZKQueryElement("parentYesChildNo", "/", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());
        String expectedString = "/a\n" + "/b\n" + "WARNING: No READ permission for /b/bb, skipping subtree\n" + "/c\n" + "/c/cc\n" + "/zookeeper/config";

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        this.zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutString = String.join("\n", queriesOutput.get(queryElement.hashCode()));
        assertEquals(expectedString, actualOutString);
    }

    @Test
    public void testExactACLTree() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        String[] aclArguments = new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" };

        ZKQuery query = zkDefaultQuery.getValueOf("exactACL");

        ZKQueryElement queryElement = new ZKQueryElement("exactACL", "/", aclArguments, query);

        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutput = String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n";

        String expectedOutput =
        this.red + "/"+this.reset+"\n" +
        "├─── "+ this.red+"/a"+this.reset+"\n" +
        "│     └─── "+this.red+"/aa" + this.reset + "\n" +
        "├─── "+this.green+"/b"+this.reset+"\n" +
        "├─── "+this.red+"/c" +this.reset+"\n" +
        "│     └─── "+this.red+"/cc" +this.reset+"\n" +
        "└─── "+this.red+"/zookeeper"+this.reset+"\n" +
        "      ├─── "+this.red+"/config"+this.reset+"\n" +
        "      └─── "+this.red+"/quota"+this.reset+"\n";

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testExactACLFind() throws Exception {
        String tempDigest = DigestAuthenticationProvider.generateDigest("user2:passw2");
        String[] aclArguments = new String[] { "digest:" + tempDigest + ":rcda", "ip:127.0.0.3:rda" };

        ZKQuery query = zkDefaultQuery.getValueOf("exactACL");

        ZKQueryElement queryElement = new ZKQueryElement("exactACL", "/", aclArguments, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        String expectedString = "/b\n" + "WARNING: No READ permission for /b/bb, skipping subtree";

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        this.zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutString = String.join("\n", queriesOutput.get(queryElement.hashCode()));
        assertEquals(expectedString, actualOutString);
    }

    @Test
    public void testRegexMatchACLTree() throws Exception {
        String[] aclArguments = new String[] { "digest:.*:.*r.*" };

        ZKQuery query = zkDefaultQuery.getValueOf("regexMatchACL");

        ZKQueryElement queryElement = new ZKQueryElement("regexMatchACL", "/", aclArguments, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutput = String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n";
        String expectedOutput = 
        this.red + "/"+this.reset+"\n" +
        "├─── "+ this.green+"/a"+this.reset+"\n" +
        "│     └─── "+this.green+"/aa" + this.reset + "\n" +
        "├─── "+this.green+"/b"+this.reset+"\n" +
        "├─── "+this.green+"/c" +this.reset+"\n" +
        "│     └─── "+this.red+"/cc" +this.reset+"\n" +
        "└─── "+this.red+"/zookeeper"+this.reset+"\n" +
        "      ├─── "+this.red+"/config"+this.reset+"\n" +
        "      └─── "+this.red+"/quota"+this.reset+"\n";

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testRegexMatchACLFind() throws Exception {
        String[] aclArguments = new String[] { "digest:.*:.*r.*" };
        ZKQuery query = zkDefaultQuery.getValueOf("regexMatchACL");

        ZKQueryElement queryElement = new ZKQueryElement("regexMatchACL", "/", aclArguments, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());
        String expectedString = "/a\n" + "/a/aa\n" + "/b\n" + "WARNING: No READ permission for /b/bb, skipping subtree\n" + "/c";        
        
        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        this.zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutString = String.join("\n", queriesOutput.get(queryElement.hashCode()));
        assertEquals(expectedString, actualOutString);
    }

    @Test
    public void testGlobMatchACLTree() throws Exception {
        String[] aclArguments = new String[] { "digest:*:*r*" };

        ZKQuery query = zkDefaultQuery.getValueOf("globMatchACL");

        ZKQueryElement queryElement = new ZKQueryElement("globMatchACL", "/", aclArguments, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutput = String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n";

        String expectedOutput = 
        this.red + "/"+this.reset+"\n" +
        "├─── "+ this.green+"/a"+this.reset+"\n" +
        "│     └─── "+this.green+"/aa" + this.reset + "\n" +
        "├─── "+this.green+"/b"+this.reset+"\n" +
        "├─── "+this.green+"/c" +this.reset+"\n" +
        "│     └─── "+this.red+"/cc" +this.reset+"\n" +
        "└─── "+this.red+"/zookeeper"+this.reset+"\n" +
        "      ├─── "+this.red+"/config"+this.reset+"\n" +
        "      └─── "+this.red+"/quota"+this.reset+"\n";

        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testGlobMatchACLFind() throws Exception {
        String[] aclArguments = new String[] { "digest:*:*r*" };
        ZKQuery query = zkDefaultQuery.getValueOf("globMatchACL");

        ZKQueryElement queryElement = new ZKQueryElement("globMatchACL", "/", aclArguments, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        String expectedString = "/a\n" + "/a/aa\n" + "/b\n" + "WARNING: No READ permission for /b/bb, skipping subtree\n" + "/c";
        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);
        this.zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
        String actualOutString = String.join("\n", queriesOutput.get(queryElement.hashCode()));
        assertEquals(expectedString, actualOutString);
    }

}
