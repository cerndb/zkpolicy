/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
public class ZKQueryCliTest {
  private TestingServer zkTestServer;
  private ZKConfig config;
  private ZKClient zkClient;

  private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @TempDir
  static File testTempDir;

  String configPath;

  @BeforeAll
  public void startZookeeper() throws Exception {
    InstanceSpec spec = new InstanceSpec(null, -1, -1, -1, true, -1, -1, -1, null);
    zkTestServer = new TestingServer(spec, true);
    config = new ZKConfig(spec.getConnectString(), 2000, "GREEN", "RED", "");
    this.zkClient = new ZKClient(config);

    // Setup the znode tree for tests
    // a subtree
    List<ACL> aclListA = new ArrayList<ACL>();
    String tempDigest = DigestAuthenticationProvider.generateDigest("user1:passw1");
    aclListA.add(new ACLAugment("digest:" + tempDigest + ":crwda").getACL());
    aclListA.add(new ACLAugment("world:anyone:drwa").getACL());
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
    fw.flush();
    fw.close();

    this.configPath = configFile.getCanonicalPath();
  }

  @BeforeEach
  public void setUpStreams() {
    outContent.reset();
    errContent.reset();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @AfterAll
  public void stopZookeeper() throws IOException, InterruptedException {
    this.zkClient.close();
    this.zkTestServer.close();
  }

  @Test
  public void testQuerySubCommand() {
    String[] args = { "-c", this.configPath, "query", "noACL", "-p", "/", "-l" };
    new CommandLine(new ZKPolicyCli()).execute(args);

    String expectedOutput = "/\n" + "WARNING: No READ permission for /b, skipping subtree\n"
        + "WARNING: No READ permission for /c, skipping subtree\n" + "/zookeeper\n" + "/zookeeper/quota\n";

    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testQuerySubCommandInvalidQueryName() {
    String[] args = { "-c", this.configPath, "query", "invalidQuery", "-p", "/", "-l" };
    new CommandLine(new ZKPolicyCli()).execute(args);

    String expectedOutput = "No such method: invalidQuery\n"
        + "Please consult the list of default queries using query -h\n";

    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testQuerySubCommandMultipleArgs() {
    String[] args = { "-c", this.configPath, "query", "regexMatchACL", "-p", "/", "-a", "world:anyone:.*a.*",
        "digest:.*", "-l" };
    new CommandLine(new ZKPolicyCli()).execute(args);

    String expectedOutput = "/a\n" + "/a/aa\n" + "WARNING: No READ permission for /b, skipping subtree\n"
        + "WARNING: No READ permission for /c, skipping subtree\n";

    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testQuerySubCommandMultipleArgsDescription() {
    String[] args = { "-c", this.configPath, "query", "regexMatchACL", "-p", "/", "-a", "world:anyone:.*a.*",
        "digest:.*", "-l", "-D" };
    new CommandLine(new ZKPolicyCli()).execute(args);

    String expectedOutput = "Description: Znodes under / that satisfy the regexMatchACL query\n"
        + " * regexMatchACL: Match znodes with ACL entries matching the passed regular expression arguments\n" + "\n"
        + "/a\n" + "/a/aa\n" + "WARNING: No READ permission for /b, skipping subtree\n"
        + "WARNING: No READ permission for /c, skipping subtree\n";

    assertEquals(expectedOutput, outContent.toString());
  }

  @Test
  public void testQuerySubCommandMultipleArgsColorDescription() {
    String[] args = { "-c", this.configPath, "query", "regexMatchACL", "-p", "/a", "-a", "world:anyone:.*a.*",
        "digest:.*", "--color-description" };
    new CommandLine(new ZKPolicyCli()).execute(args);

    String matchColorVal = ZKPolicyDefs.Colors.valueOf(zkClient.getZKPConfig().getMatchColor()).getANSIValue();
    String matchColor = zkClient.getZKPConfig().getMatchColor();
    String misMatchColorVal = ZKPolicyDefs.Colors.valueOf(zkClient.getZKPConfig().getMismatchColor()).getANSIValue();
    String misMatchColor = zkClient.getZKPConfig().getMismatchColor();
    String resetColorVal = ZKPolicyDefs.Colors.RESET.getANSIValue();

    String expectedOutput = "* " + matchColorVal + matchColor + resetColorVal + ": znodes matching the query\n" + "* "
        + misMatchColorVal + misMatchColor + resetColorVal + ": znodes not matching the query\n" + "\n" + matchColorVal
        + "/a" + resetColorVal + "\n" + "└─── " + matchColorVal + "/aa" + resetColorVal + "\n";

    assertEquals(expectedOutput, outContent.toString());
  }
}