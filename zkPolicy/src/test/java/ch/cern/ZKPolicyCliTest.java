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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.curator.test.TestingServer;
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
public class ZKPolicyCliTest {
  private TestingServer zkTestServer;

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
    java.lang.System.clearProperty("java.security.auth.login.config");
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    java.lang.System.clearProperty("java.security.auth.login.config");
  }

  @AfterAll
  public void stopZookeeper() throws InterruptedException, IOException {
    this.zkTestServer.close();
  }

  @Test
  public void testJaasFromConfig() {
    String[] args = {"-c", this.configPath, "tree", "-p", "/"};

    new CommandLine(new ZKPolicyCli()).execute(args);

    assertNotEquals("", outContent.toString());
    assertEquals("/path/to/jaas.conf", java.lang.System.getProperty("java.security.auth.login.config"));
  }

}