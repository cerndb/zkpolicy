/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKConfigTest {
  TestingServer zkTestServer;
  CuratorFramework cli;

  @TempDir
  File testTempDir;

  @BeforeEach
  public void setUpStreams() {
    java.lang.System.clearProperty("java.security.auth.login.config");
  }

  @AfterEach
  public void restoreStreams() {
    java.lang.System.clearProperty("java.security.auth.login.config");
  }

  @Test
  public void testZKConfigConstructor() throws Exception {
    File file = new File(testTempDir, "conf_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("timeout: 2000\n");
    fw.write("zkServers: \"127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181\"\n");
    fw.write("matchColor: \"GREEN\"\n");
    fw.write("mismatchColor: \"RED\"\n");
    fw.write("jaas: \"/path/to/jaas.conf\"\n");
    fw.flush();
    fw.close();

    ZKConfig zkConfig = new ZKConfig(file);
    assertAll(() -> assertEquals("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", zkConfig.getZkServers()),
        () -> assertEquals(2000, zkConfig.getTimeout()), () -> assertEquals("GREEN", zkConfig.getMatchColor()),
        () -> assertEquals("RED", zkConfig.getMismatchColor()),
        () -> assertEquals("/path/to/jaas.conf", zkConfig.getJaas()));
  }

  @Test
  public void testSetPropertyLog4jNull() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    java.lang.System.clearProperty("log4j.configuration");
    new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor, null);
    assertEquals("", java.lang.System.getProperty("log4j.configuration", ""));
  }

  @Test
  public void testSetPropertyJaasFromConfigFile() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "/path/to/jaas.conf";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas);
    zkConfig.setPropertyJaas();
    assertEquals("/path/to/jaas.conf", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testSetPropertyJaasFromCliOrEnv() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "/path/to/jaas.conf";
    java.lang.System.clearProperty("java.security.auth.login.config");
    java.lang.System.setProperty("java.security.auth.login.config", jaas);
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        null);
    zkConfig.setPropertyJaas();
    assertEquals("/path/to/jaas.conf", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testSetPropertyJaasNull() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        null);
    zkConfig.setPropertyJaas();
    assertEquals("", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testSetPropertyJaasEmpty() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas);
    zkConfig.setPropertyJaas();
    assertEquals("", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testConstructor() throws Exception {
    assertNotNull(new ZKConfig());
  }
}
