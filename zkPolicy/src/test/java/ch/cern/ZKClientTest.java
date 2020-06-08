package ch.cern;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.ZooKeeper.States;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKClientTest {
  TestingServer zkTestServer;
  ZKConfig config;

  @BeforeAll
  public void startZookeeper() throws Exception {
    // Choose an available port
    zkTestServer = new TestingServer();
  }

  @Test
  public void testConnect() throws Exception {
    config = new ZKConfig(zkTestServer.getConnectString(), 2000, "GREEN", "RED", "");
    ZKClient zkClient = new ZKClient(config);
    Assertions.assertEquals(States.CONNECTED, zkClient.getState());
    zkClient.close();
  }

}
