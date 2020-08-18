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

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
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
public class ZKTreeCliTest {
  private TestingServer zkTestServer;
  private ZKConfig config;
  private ZKClient zkClient;
  private String resetColor;
  private String whiteColor;

  private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

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

    this.whiteColor = ZKPolicyDefs.Colors.WHITE.getANSIValue();
    this.resetColor = ZKPolicyDefs.Colors.RESET.getANSIValue();

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
  public void testTreeSubCommand() throws IOException {
    String[] args = { "-c", this.configPath, "tree", "-p", "/" };

    new CommandLine(new ZKPolicyCli()).execute(args);
    String expectedOutput = String.join("\n", this.whiteColor + "/" + this.resetColor,
        "├─── " + this.whiteColor + "/a" + this.resetColor, "│     └─── " + this.whiteColor + "/aa" + this.resetColor,
        "├─── " + this.whiteColor + "/b" + this.resetColor, "│     └─── " + this.whiteColor + "/bb" + this.resetColor,
        "├─── " + this.whiteColor + "/c" + this.resetColor, "│     └─── " + this.whiteColor + "/cc" + this.resetColor,
        "└─── " + this.whiteColor + "/zookeeper" + this.resetColor,
        "      ├─── " + this.whiteColor + "/config" + this.resetColor,
        "      └─── " + this.whiteColor + "/quota" + this.resetColor, "", "");

    assertEquals(expectedOutput, outContent.toString());
  }
}