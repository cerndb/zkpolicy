package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeTest {
    ZKConfig config;
    ZKClient zkClient;
    ZKTree zkTree;
    TestingServer zkTestServer;
    String colorMatch;
    String colorNoMatch;
    String colorReset;
    ZKDefaultQuery zkDefaultQuery;

    @TempDir
    static File testTempDir;

    String configPath;

    @BeforeAll
    public void startZookeeper() throws Exception {
        // Choose an available port
        zkTestServer = new TestingServer();
        config = new ZKConfig(zkTestServer.getConnectString(), 2000, "GREEN", "RED", "");
        this.zkClient = new ZKClient(config);

        // Setup the znode tree for tests
        // a subtree
        ArrayList<ACL> aclList = new ArrayList<ACL>();
        aclList.add(new ACL(ZooDefs.Perms.ALL, new Id("world", "anyone")));
        zkClient.create("/a", "a".getBytes(), aclList, CreateMode.PERSISTENT);

        zkClient.create("/a/aa", "aa".getBytes(), aclList, CreateMode.PERSISTENT);

        // b subtree
        zkClient.create("/b", "b".getBytes(), aclList, CreateMode.PERSISTENT);

        zkClient.create("/b/bb", "bb".getBytes(), aclList, CreateMode.PERSISTENT);

        // c subtree
        // auth scheme ignores the passed id and matches the current authentication
        zkClient.create("/c", "c".getBytes(), aclList, CreateMode.PERSISTENT);
        
        zkClient.create("/c/cc", "cc".getBytes(), aclList, CreateMode.PERSISTENT);

        this.colorMatch = ZKPolicyDefs.Colors.GREEN.getANSIValue();
        this.colorNoMatch = ZKPolicyDefs.Colors.RED.getANSIValue();
        this.colorReset = ZKPolicyDefs.Colors.RESET.getANSIValue();

        // Setup config file
        File configFile = new File(testTempDir, "conf_tmp.yml");

        FileWriter fw = new FileWriter(configFile);
        fw.write("---\n");
        fw.write("timeout: 2000\n");
        fw.write("zkServers: \""+ zkTestServer.getConnectString() +"\"\n");
        fw.write("matchColor: \"GREEN\"\n");
        fw.write("mismatchColor: \"RED\"\n");
        fw.write("jaas: \"/path/to/jaas.conf\"\n");
        fw.flush();
        fw.close();

        this.configPath = configFile.getCanonicalPath();
        this.config = new ZKConfig(new File(this.configPath)); 
        this.zkClient = new ZKClient(config);
        this.zkTree  = new ZKTree(zkClient);
        zkDefaultQuery = new ZKDefaultQuery();
    }

    @Test
    public void testColorCodeExplanation() throws Exception {
        String expectedOutput = "* " + this.colorMatch + "GREEN" + this.colorReset + ": znodes matching the query\n"+
        "* " + this.colorNoMatch + "RED" + this.colorReset + ": znodes not matching the query\n";

        assertEquals(expectedOutput, zkTree.colorCodeExplanation());
    }

    @Test
    public void testQuerySuccess() throws Exception {
        // Execute query with invalid rootPath
        ZKQuery query = zkDefaultQuery.getValueOf("parentYesChildNo");
        ZKQueryElement queryElement = new ZKQueryElement("parentYesChildNo", "/zookeeper", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);

        this.zkTree.queryFind("/zookeeper", queriesList, queriesOutput);

        String expectedOutputFind = "/zookeeper/config\n";

        assertEquals(expectedOutputFind, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");

        // Clean queries Output buffer 
        queriesOutput.remove(queryElement.hashCode());
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        // ParentYesChildNo 
        queriesList.add(queryElement);
        this.zkTree.queryTree(queryElement.getRootPath(), queriesList, queriesOutput);

        // Query is deleted as invalid so add it once again
        this.zkTree.queryTree("/zookeeper", queriesList, queriesOutput);

        String expectedOutputTree = this.colorReset + "/zookeeper\n" + 
        this.colorNoMatch + "├─── " + this.colorReset + "/config\n" + 
        this.colorMatch + "└─── " + this.colorReset + "/quota\n";

        assertEquals(expectedOutputTree, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
    }

    @Test
    public void testNotExistingRootPath() throws Exception {
        // Execute query with invalid rootPath
        ZKQuery query = zkDefaultQuery.getValueOf("noACL");
        ZKQueryElement queryElement = new ZKQueryElement("noACL", "/invalid-path", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);

        this.zkTree.queryFind(queryElement.getRootPath(), queriesList, queriesOutput);

        String expectedOutput = "The path /invalid-path does not exist.\n";

        assertEquals(expectedOutput, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");

        // Clean queries Output buffer 
        queriesOutput.remove(queryElement.hashCode());
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        // Query is deleted as invalid so add it once again
        queriesList.add(queryElement);
        this.zkTree.queryTree(queryElement.getRootPath(), queriesList, queriesOutput);

        assertEquals(expectedOutput, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
    }

    @Test
    public void testInvalidRootPath() throws Exception {
        // Execute query with invalid rootPath
        ZKQuery query = zkDefaultQuery.getValueOf("noACL");
        ZKQueryElement queryElement = new ZKQueryElement("noACL", "invalid-path", null, query);
        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
        queriesList.add(queryElement);

        this.zkTree.queryFind(queryElement.getRootPath(), queriesList, queriesOutput);

        String expectedOutput = "Invalid rootpath invalid-path : Path must start with / character\n";

        assertEquals(expectedOutput, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");

        // Clean queries Output buffer 
        queriesOutput.remove(queryElement.hashCode());
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        // Query is deleted as invalid so add it once again
        queriesList.add(queryElement);
        this.zkTree.queryTree(queryElement.getRootPath(), queriesList, queriesOutput);

        assertEquals(expectedOutput, String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
    }


}
