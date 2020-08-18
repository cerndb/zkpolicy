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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingCluster;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
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
public class ZKEnforceCliTest {
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

  @BeforeAll
  public void startZookeeper() throws Exception {
    // Setup test server to ignore ACL for report checking
    InstanceSpec spec = new InstanceSpec(null, -1, -1, -1, true, -1, -1, -1, null);

    // Choose an available port
    zkTestServer = new TestingServer(spec, true);
    config = new ZKConfig(spec.getConnectString(), 2000, "GREEN", "RED", "");
    this.zkClient = new ZKClient(config);

    // Setup the znode tree for tests
    // a subtree
    List<ACL> aclListA = new ArrayList<ACL>();
    aclListA.add(new ACLAugment("world:anyone:cdrwa").getACL());
    zkClient.create("/a", "a".getBytes(), aclListA, CreateMode.PERSISTENT);
    zkClient.create("/a/aa", "aa".getBytes(), aclListA, CreateMode.PERSISTENT);

    // b subtree
    List<ACL> aclListB = new ArrayList<ACL>();
    aclListB.add(new ACLAugment("world:anyone:cdrwa").getACL());
    aclListB.add(new ACLAugment("ip:127.0.0.3:rda").getACL());

    zkClient.create("/b", "b".getBytes(), aclListB, CreateMode.PERSISTENT);

    List<ACL> aclListBB = new ArrayList<ACL>();
    aclListBB.add(new ACLAugment("world:anyone:cdrwa").getACL());
    zkClient.create("/b/bb", "bb".getBytes(), aclListBB, CreateMode.PERSISTENT);

    // c subtree
    // auth scheme ignores the passed id and matches the current authentication
    List<ACL> aclListC = new ArrayList<ACL>();
    aclListC.add(new ACLAugment("world:anyone:cdrwa").getACL());
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

    // Setup service policy files
    File servicePolicyOneFile = new File(testTempDir, "serviceOne.yml");

    fw = new FileWriter(servicePolicyOneFile);
    fw.write("---\n");
    fw.write("policies:\n");
    fw.write("  - title: \"Service One policy\"\n");
    fw.write("    query:\n");
    fw.write("      name: \"regexMatchPath\"\n");
    fw.write("      rootPath: \"/a\"\n");
    fw.write("      args:\n");
    fw.write("        - \"/a.*\"\n");
    fw.write("    append: false\n");
    fw.write("    acls:\n");
    fw.write("      - \"world:anyone:r\"\n");
    fw.flush();
    fw.close();

    File servicePolicyTwoFile = new File(testTempDir, "serviceTwo.yml");

    fw = new FileWriter(servicePolicyTwoFile);
    fw.write("---\n");
    fw.write("policies:\n");
    fw.write("  - title: \"Service Two policy\"\n");
    fw.write("    query:\n");
    fw.write("      name: \"regexMatchPath\"\n");
    fw.write("      rootPath: \"/b\"\n");
    fw.write("      args:\n");
    fw.write("        - \"/b.*\"\n");
    fw.write("    append: false\n");
    fw.write("    acls:\n");
    fw.write("      - \"world:anyone:cdr\"\n");
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

  @AfterAll
  public void stopZookeeper() throws IOException, InterruptedException {
    //this.zkClient.close();
    this.zkTestServer.close();
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  @Test
  public void testEnforceMultipleServicesSubCommand() throws IOException, KeeperException, InterruptedException {

    String[] args = { "-c", configPath, "enforce", "-D", testTempDir.toString() , "-s", "serviceOne", "-s", "serviceTwo" };

    new CommandLine(new ZKPolicyCli()).execute(args);

    List<ACL> expectedList = new ArrayList<ACL>();
    expectedList.add(new ACLAugment("world:anyone:r").getACL());
    List<ACL> alteredList = this.zkClient.getACL("/a", null);
    assertEquals(expectedList, alteredList);

    expectedList = new ArrayList<ACL>();
    expectedList.add(new ACLAugment("world:anyone:cdr").getACL());
    alteredList = this.zkClient.getACL("/b", null);
    assertEquals(expectedList, alteredList);
  }
}