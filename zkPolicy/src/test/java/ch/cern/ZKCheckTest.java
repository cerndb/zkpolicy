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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKCheckTest {
  TestingServer zkTestServer;
  ZKConfig config;
  ZKCheck zkCheck;
  ZKClient zkClient;
  String green;
  String red;
  String reset;
  ZKDefaultQuery zkDefaultQuery;

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

    this.green = ZKPolicyDefs.Colors.GREEN.getANSIValue();
    this.red = ZKPolicyDefs.Colors.RED.getANSIValue();
    this.reset = ZKPolicyDefs.Colors.RESET.getANSIValue();

    this.zkCheck = new ZKCheck(this.zkClient);
  }

  @AfterAll
  public void stopZookeeper() throws IOException, InterruptedException {
    this.zkClient.close();
    this.zkTestServer.close();
  }

  @Test
  public void testCheck()
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchFieldException, KeeperException, InterruptedException, NoSuchAlgorithmException {

    List<String> checkACLs = new ArrayList<String>();
    checkACLs.add("world:anyone:cdrwa");
    ZKCheckElement checkElement = new ZKCheckElement("TestTitle", "/", "/.*", false, checkACLs);
    List<ZKCheckElement> checksList = new ArrayList<ZKCheckElement>();
    Hashtable<Integer, List<String>> checksOutput = new Hashtable<Integer, List<String>>();

    checksList.add(checkElement);
    checksOutput.put(checkElement.hashCode(), new ArrayList<String>());

    zkCheck.check("/", checksList, checksOutput);

    String tempDigest1 = DigestAuthenticationProvider.generateDigest("user1:passw1");
    String tempDigest2 = DigestAuthenticationProvider.generateDigest("user2:passw2");

    String expectedOutput = "/ : PASS\n" + "/a : FAIL (actual: digest:" + tempDigest1 + ":cdrwa)\n"
        + "/a/aa : FAIL (actual: digest:" + tempDigest1 + ":cdrwa)\n" + "/b : FAIL (actual: digest:"
        + tempDigest2 + ":cdra, ip:127.0.0.3:dra)\n" + "/b/bb : FAIL (No permission for subtree)\n"
        + "/c : FAIL (actual: digest:" + tempDigest2 + ":cdra, digest:" + tempDigest1 + ":cdra)\n"
        + "/c/cc : PASS\n" + "/zookeeper : PASS\n" + "/zookeeper/config : FAIL (actual: world:anyone:r)\n"
        + "/zookeeper/quota : PASS\n";

    assertEquals(expectedOutput, String.join("\n", checksOutput.get(checkElement.hashCode())) + "\n");
  }

  @Test
  public void testNotExistingRootPath() throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchFieldException, KeeperException, InterruptedException {
    List<String> checkACLs = new ArrayList<String>();
    checkACLs.add("world:anyone:cdrwa");
    ZKCheckElement checkElement = new ZKCheckElement("TestTitle", "/not-existing-path", "/.*", false, checkACLs);
    List<ZKCheckElement> checksList = new ArrayList<ZKCheckElement>();
    Hashtable<Integer, List<String>> checksOutput = new Hashtable<Integer, List<String>>();

    checksList.add(checkElement);
    checksOutput.put(checkElement.hashCode(), new ArrayList<String>());
    zkCheck.check("/", checksList, checksOutput);

    String expectedOutput = "The path /not-existing-path does not exist.\n";

    assertEquals(expectedOutput, String.join("\n", checksOutput.get(checkElement.hashCode())) + "\n");
  }

  @Test
  public void testInvalidRootPath() throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchFieldException, KeeperException, InterruptedException {
    List<String> checkACLs = new ArrayList<String>();
    checkACLs.add("world:anyone:cdrwa");
    ZKCheckElement checkElement = new ZKCheckElement("TestTitle", "invalid-root-path", "/.*", false, checkACLs);
    List<ZKCheckElement> checksList = new ArrayList<ZKCheckElement>();
    Hashtable<Integer, List<String>> checksOutput = new Hashtable<Integer, List<String>>();

    checksList.add(checkElement);
    checksOutput.put(checkElement.hashCode(), new ArrayList<String>());

    zkCheck.check("/", checksList, checksOutput);

    String expectedOutput = "Invalid rootpath invalid-root-path : Path must start with / character\n";

    assertEquals(expectedOutput, String.join("\n", checksOutput.get(checkElement.hashCode())) + "\n");
  }
}