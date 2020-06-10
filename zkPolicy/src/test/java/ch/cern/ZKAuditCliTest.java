package ch.cern;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKAuditCliTest {
  private TestingServer zkTestServer;
  private ZKConfig config;
  private ZKClient zkClient;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @TempDir
  static File testTempDir;

  String configPath;
  String auditConfigPath;

  @BeforeAll
  public void startZookeeper() throws Exception {
    // Setup test server to ignore ACL for report checking
    Map<String, Object> customProperties = new HashMap<String, Object>();
    customProperties.put("skipACL", "yes");
    InstanceSpec spec = new InstanceSpec(null, -1, -1, -1, true, 1, -1, -1, customProperties);

    // Choose an available port
    zkTestServer = new TestingServer(spec, true);
    config = new ZKConfig(zkTestServer.getConnectString(), 2000, "GREEN", "RED", "");
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

    // Setup config file
    File configFile = new File(testTempDir, "conf_tmp.yml");

    FileWriter fw = new FileWriter(configFile);
    fw.write("---\n");
    fw.write("timeout: 2000\n");
    fw.write("zkServers: \"" + zkTestServer.getConnectString() + "\"\n");
    fw.write("matchColor: \"GREEN\"\n");
    fw.write("mismatchColor: \"RED\"\n");
    fw.write("jaas: \"/path/to/jaas.conf\"\n");
    fw.flush();
    fw.close();

    // Setup audit config file
    File auditConfigFile = new File(testTempDir, "conf_audit.yml");

    fw = new FileWriter(auditConfigFile);
    fw.write("---\n");
    fw.write("sections:\n");
    fw.write("  generalInformation: true\n");
    fw.write("  fourLetterWordCommands: true\n");
    fw.write("  queryResults: true\n");
    fw.write("  checkResults: true\n");
    fw.write("  aclOverview: true\n");
    fw.write("queries:\n");
    fw.write(" -\n");
    fw.write("  name: \"globMatchACL\"\n");
    fw.write("  rootPath: \"/a\"\n");
    fw.write("  args:\n");
    fw.write("   - \"digest:*:*\"\n");

    fw.write(" -\n");
    fw.write("  name: \"exactACL\"\n");
    fw.write("  rootPath: \"/zookeeper/quota\"\n");
    fw.write("  args:\n");
    fw.write("   - \"world:anyone:r\"\n");

    fw.write(" -\n");
    fw.write("  name: \"noACL\"\n");
    fw.write("  rootPath: \"/b\"\n");

    fw.write(" -\n");
    fw.write("  name: \"globMatchACL\"\n");
    fw.write("  rootPath: \"/b\"\n");
    fw.write("  args:\n");
    fw.write("   - \"*:*:*\"\n");

    fw.write(" -\n");
    fw.write("  name: \"regexMatchACL\"\n");
    fw.write("  rootPath: \"/\"\n");
    fw.write("  args:\n");
    fw.write("   - \"ip:.*:.*\"\n");

    fw.flush();
    fw.close();

    this.configPath = configFile.getCanonicalPath();
    this.auditConfigPath = auditConfigFile.getCanonicalPath();
  }

  @AfterAll
  public void stopZookeeper() throws IOException, InterruptedException {
    this.zkClient.close();
  }


  @BeforeEach
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testAuditSubCommand() throws IOException {
    String[] args = { "-c", configPath, "audit", "-i", auditConfigPath };

    new CommandLine(new ZKPolicyCli()).execute(args);
    assertTrue(!outContent.toString().isEmpty());
  }
}