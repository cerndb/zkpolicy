package ch.cern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
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

  @Test
  public void testZKConfigConstructor() throws Exception {
    File file = new File(testTempDir, "conf_tmp.yml");

    FileWriter fw = new FileWriter(file);
    fw.write("---\n");
    fw.write("timeout: 2000\n");
    fw.write("zkservers: \"127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181\"\n");
    fw.write("matchcolor: \"GREEN\"\n");
    fw.write("mismatchcolor: \"RED\"\n");
    fw.write("jaas: \"/path/to/jaas.conf\"\n");
    fw.write("log4j: \"/path/to/log4j.properties\"");
    fw.flush();
    fw.close();

    ZKConfig zkConfig = new ZKConfig(file);
    assertAll(() -> assertEquals("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", zkConfig.getZkservers()),
        () -> assertEquals(2000, zkConfig.getTimeout()), () -> assertEquals("GREEN", zkConfig.getMatchcolor()),
        () -> assertEquals("RED", zkConfig.getMismatchcolor()),
        () -> assertEquals("/path/to/jaas.conf", zkConfig.getJaas()),
        () -> assertEquals("/path/to/log4j.properties", zkConfig.getLog4j()));
  }

  @Test
  public void testSetPropertyLog4j() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "/path/to/jaas.conf";
    String log4j = "/path/to/log4j.properties";
    java.lang.System.clearProperty("log4j.configuration");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas, log4j);
    zkConfig.setPropertyLog4j();
    assertEquals("file:/path/to/log4j.properties", java.lang.System.getProperty("log4j.configuration", ""));
  }

  @Test
  public void testSetPropertyLog4jNull() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    java.lang.System.clearProperty("log4j.configuration");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        null, null);
    zkConfig.setPropertyLog4j();
    assertEquals("", java.lang.System.getProperty("log4j.configuration", ""));
  }

  @Test
  public void testSetPropertyLog4jEmpty() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "";
    String log4j = "";
    java.lang.System.clearProperty("log4j.configuration");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas, log4j);
    zkConfig.setPropertyLog4j();
    assertEquals("", java.lang.System.getProperty("log4j.configuration", ""));
  }

  @Test
  public void testSetPropertyJaas() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "/path/to/jaas.conf";
    String log4j = "/path/to/log4j.properties";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas, log4j);
    zkConfig.setPropertyJaas();
    assertEquals("/path/to/jaas.conf", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testSetPropertyJaasNull() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        null, null);
    zkConfig.setPropertyJaas();
    assertEquals("", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testSetPropertyJaasEmpty() throws Exception {
    String matchcolor = "GREEN";
    String mismatchcolor = "RED";
    String jaas = "";
    String log4j = "";
    java.lang.System.clearProperty("java.security.auth.login.config");
    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolor, mismatchcolor,
        jaas, log4j);
    zkConfig.setPropertyJaas();
    assertEquals("", java.lang.System.getProperty("java.security.auth.login.config", ""));
  }

  @Test
  public void testConstructor() throws Exception{
    assertNotNull(new ZKConfig());
  }
}
