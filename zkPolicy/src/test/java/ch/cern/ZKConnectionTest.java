package ch.cern;

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKConnectionTest {
    TestingServer zkTestServer;
    CuratorFramework cli;

    @BeforeAll
    public void startZookeeper() throws Exception {
        // Choose an available port
        zkTestServer = new TestingServer(2281);
    }

    @AfterAll
    public void stopZookeeper() throws IOException {
        zkTestServer.stop();
    }

    @Test
    public void testConnect() throws Exception {
        ZKConnection zkConnection = new ZKConnection();
        ZooKeeper zkeeper = zkConnection.connect("127.0.0.1:2281", 2000);
        Assertions.assertEquals(States.CONNECTED, zkeeper.getState());
    }

    @Test
    public void testClose() throws Exception {
        ZKConnection zkConnection = new ZKConnection();
        ZooKeeper zkeeper = zkConnection.connect("127.0.0.1:2281", 2000);
        zkConnection.close();
        Assertions.assertEquals(States.CLOSED, zkeeper.getState());
    }
}
