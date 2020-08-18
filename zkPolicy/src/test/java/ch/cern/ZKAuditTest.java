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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;
import ch.cern.ZKPolicyDefs.Queries;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKAuditTest {

  TestingServer zkTestServer;
  CuratorFramework cli;
  ZKClient zkClient;
  ZKConfig config;

  @TempDir
  File testTempDir;

  @BeforeAll
  public void startZookeeper() throws Exception {
    // Choose an available port
    zkTestServer = new TestingServer();

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
  }

  @AfterAll
  public void stopZookeeper() throws IOException, InterruptedException {
    this.zkClient.close();
    this.zkTestServer.close();
  }

  @Test
  public void testGroupQueriesByRootPath() throws Exception {
    File file = new File(testTempDir, "audit_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("queries:\n");
    fw.write(" -\n");
    fw.write("  name: \"globMatchACL\"\n");
    fw.write("  rootPath: \"/\"\n");
    fw.write("  args:\n");
    fw.write("   - \"digest:*:*\"\n");

    fw.write(" -\n");
    fw.write("  name: \"exactACL\"\n");
    fw.write("  rootPath: \"/zookeeper/quota\"\n");
    fw.write("  args:\n");
    fw.write("   - \"world:anyone:r\"\n");

    fw.write(" -\n");
    fw.write("  name: \"noACL\"\n");
    fw.write("  rootPath: \"/a\"\n");

    fw.write(" -\n");
    fw.write("  name: \"parentYesChildNo\"\n");
    fw.write("  rootPath: \"/hbase\"\n");

    fw.write(" -\n");
    fw.write("  name: \"regexMatchACL\"\n");
    fw.write("  rootPath: \"/zookeeper\"\n");
    fw.write("  args:\n");
    fw.write("   - \"sasl:.*:.*\"\n");

    fw.flush();
    fw.close();

    ZKAudit zkAudit = new ZKAudit(this.zkClient, file);
    assertNotNull(zkAudit);
    zkAudit.groupQueriesByRootPath();

    Set<String> rootPathKeys = zkAudit.getRootPathKeys();
    assertNotNull(rootPathKeys);

    assertTrue(rootPathKeys.containsAll(new ArrayList<String>(Arrays.asList("/", "/zookeeper/quota", "/a", "/hbase"))));
  }

  @Test
  public void testGetACLOverview() throws Exception {
    File file = new File(testTempDir, "audit_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("queries:\n");
    fw.write(" -\n");
    fw.write("  name: \"globMatchACL\"\n");
    fw.write("  rootPath: \"/\"\n");
    fw.write("  args:\n");
    fw.write("   - \"digest:*:*\"\n");

    fw.write(" -\n");
    fw.write("  name: \"exactACL\"\n");
    fw.write("  rootPath: \"/zookeeper/quota\"\n");
    fw.write("  args:\n");
    fw.write("   - \"world:anyone:r\"\n");

    fw.write(" -\n");
    fw.write("  name: \"noACL\"\n");
    fw.write("  rootPath: \"/a\"\n");

    fw.write(" -\n");
    fw.write("  name: \"parentYesChildNo\"\n");
    fw.write("  rootPath: \"/hbase\"\n");

    fw.write(" -\n");
    fw.write("  name: \"regexMatchACL\"\n");
    fw.write("  rootPath: \"/zookeeper\"\n");
    fw.write("  args:\n");
    fw.write("   - \"sasl:.*:.*\"\n");

    fw.flush();
    fw.close();

    ZKAudit zkAudit = new ZKAudit(this.zkClient, file);
    assertNotNull(zkAudit);

    String expectedOutput = "Permission overview for ZooKeeper Tree\n" + "\n" + "/ - world:anyone:cdrwa\n"
        + "/a - digest:user1:fmzXXlXqk3oTcBzJlNngkWBzCVI=:cdrwa\n"
        + "/a/aa - digest:user1:fmzXXlXqk3oTcBzJlNngkWBzCVI=:cdrwa\n"
        + "/b - digest:user2:MP2IkrliHO/f+GSvcEPq1SHzoYM=:cdra, ip:127.0.0.3:dra\n"
        + "Warning: No READ permission for /b/bb, skipping this subtree\n"
        + "/c - digest:user2:MP2IkrliHO/f+GSvcEPq1SHzoYM=:cdra, digest:user1:fmzXXlXqk3oTcBzJlNngkWBzCVI=:cdra\n"
        + "/c/cc - world:anyone:cdrwa\n" + "/zookeeper - world:anyone:cdrwa\n" + "/zookeeper/config - world:anyone:r\n"
        + "/zookeeper/quota - world:anyone:cdrwa\n";

    assertEquals(expectedOutput, zkAudit.getACLOverview());
  }

  @Test
  public void testGenerateHumanReadableAuditReport() throws Exception {
    File file = new File(testTempDir, "audit_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
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
    fw.write("  rootPath: \"/zookeeper\"\n");
    fw.write("  args:\n");
    fw.write("   - \"sasl:.*:.*\"\n");

    fw.flush();
    fw.close();

    ZKAudit zkAudit = new ZKAudit(this.zkClient, file);
    assertNotNull(zkAudit);

    String expectedOutput = String.join("\n", "", "Query: globMatchACL", "Root Path: /a", "Arguments:", "- digest:*:*",
        "Description: Znodes under /a that satisfy the globMatchACL query",
        " * globMatchACL: " + Queries.GLOB_MATCH_ACL_DESCRIPTION, "", "Result:", "/a", "/a/aa", "",
        "---------------------------------------------------------------------", "", "Query: exactACL",
        "Root Path: /zookeeper/quota", "Arguments:", "- world:anyone:r",
        "Description: Znodes under /zookeeper/quota that satisfy the exactACL query",
        " * exactACL: " + Queries.EXACT_ACL_DESCRIPTION, "", "Result:", "", "",
        "---------------------------------------------------------------------", "", "Query: noACL", "Root Path: /b",
        "Description: Znodes under /b that satisfy the noACL query", " * noACL: " + Queries.NO_ACL_DESCRIPTION, "",
        "Result:", "WARNING: No READ permission for /b/bb, skipping subtree", "",
        "---------------------------------------------------------------------", "", "Query: globMatchACL",
        "Root Path: /b", "Arguments:", "- *:*:*", "Description: Znodes under /b that satisfy the globMatchACL query",
        " * globMatchACL: " + Queries.GLOB_MATCH_ACL_DESCRIPTION, "", "Result:", "/b",
        "WARNING: No READ permission for /b/bb, skipping subtree", "",
        "---------------------------------------------------------------------", "", "Query: regexMatchACL",
        "Root Path: /zookeeper", "Arguments:", "- sasl:.*:.*",
        "Description: Znodes under /zookeeper that satisfy the regexMatchACL query",
        " * regexMatchACL: " + Queries.REGEX_MATCH_ACL_DESCRIPTION, "", "Result:", "", "",
        "---------------------------------------------------------------------", "");

    assertEquals(expectedOutput, zkAudit.generateQueriesSection());
  }

  @Test
  public void testNoNodesFoundForPathPatternCheck() throws Exception {
    File file = new File(testTempDir, "audit_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("checks:\n");
    fw.write("  - title: \"Check 1 title\"\n");
    fw.write("    rootPath: \"/a\"\n");
    fw.write("    pathPattern: \"/noexistingpattern\"\n");
    fw.write("    negate: false\n");
    fw.write("    acls:\n");
    fw.write("      - \"world:anyone:r\"\n");

    fw.flush();
    fw.close();

    ZKAudit zkAudit = new ZKAudit(this.zkClient, file);
    assertNotNull(zkAudit);

    String expectedOutput = String.join("\n", "", "Check: Check 1 title", "Root Path: /a",
        "Path Pattern: /noexistingpattern", "Arguments:", "- world:anyone:r",
        "Description: " + String.format(ZKPolicyDefs.Check.DESCRIPTION_FORMAT, "/a", "/noexistingpattern"),
        "- world:anyone:r", "", "Result: PASS", "No znodes matching the requested path pattern found.",
        ZKPolicyDefs.TerminalConstants.subSectionSeparator, "", "Overall Check Result: PASS", "");

    assertEquals(expectedOutput, zkAudit.generateChecksSection());
  }

  @Test
  public void testInvalidRootPathCheck() throws Exception {
    File file = new File(testTempDir, "audit_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("checks:\n");
    fw.write("  - title: \"Check 1 title\"\n");
    fw.write("    rootPath: \"/invalidRootPath\"\n");
    fw.write("    pathPattern: \".*\"\n");
    fw.write("    negate: false\n");
    fw.write("    acls:\n");
    fw.write("      - \"world:anyone:r\"\n");

    fw.flush();
    fw.close();

    ZKAudit zkAudit = new ZKAudit(this.zkClient, file);
    assertNotNull(zkAudit);

    String expectedOutput = String.join("\n", "", "Check: Check 1 title", "Root Path: /invalidRootPath",
        "Path Pattern: .*", "Arguments:", "- world:anyone:r",
        "Description: " + String.format(ZKPolicyDefs.Check.DESCRIPTION_FORMAT, "/invalidRootPath", ".*"),
        "- world:anyone:r", "", "Result: FAIL", "The path /invalidRootPath does not exist.", "",
        ZKPolicyDefs.TerminalConstants.subSectionSeparator, "", "Overall Check Result: FAIL", "", "PASS: 0, FAIL: 1",
        "");

    assertEquals(expectedOutput, zkAudit.generateChecksSection());
  }
}
