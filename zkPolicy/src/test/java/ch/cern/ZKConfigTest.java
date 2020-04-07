package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKConfigTest {
  TestingServer zkTestServer;
  CuratorFramework cli;

  @Test
  public void testGetZKServers() throws Exception {
    String matchcolorname = "green";
    String matchcolorvalue = "\u001B[32m";
    String mismatchcolorname = "red";
    String mismatchcolorvalue = "\u001B[31m";

    ZKConfig zkConfig = new ZKConfig("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", 2000, matchcolorname,
        matchcolorvalue, mismatchcolorname, mismatchcolorvalue);
    assertEquals("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", zkConfig.getZkservers());
    assertEquals(2000, zkConfig.getTimeout());
  }

  @Test
  public void testZKConfigSetters() throws Exception {
    File file;
    try {
      file = File.createTempFile("conf_tmp.yaml", null);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
    try {
      FileWriter fw = new FileWriter("conf_tmp.yaml");
      fw.write("---\n");
      fw.write("timeout: 2000\n");
      fw.write("zkservers: \"127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181\"\n");
      fw.write("matchcolorname: \"green\"\n");
      fw.write("matchcolorvalue: \"\\u001B[32m\"\n");
      fw.write("mismatchcolorname: \"red\"\n");
      fw.write("mismatchcolorvalue: \"\\u001B[31m\"\n");
      fw.close();

      file = new File("conf_tmp.yaml");
      System.out.println(file.length());
      ObjectMapper om = new ObjectMapper(new YAMLFactory());
      ZKConfig zkConfig = om.readValue(file, ZKConfig.class);
      assertEquals("127.0.0.1:2183,127.0.0.1:2182,127.0.0.1:2181", zkConfig.getZkservers());
      assertEquals(2000, zkConfig.getTimeout());
    } finally {
      if (file.isFile() && !file.delete()) {
        throw new Exception("Failed to delete conf_tmp.yaml file");
      }
    }
  }
}
