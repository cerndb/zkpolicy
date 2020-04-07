package ch.cern;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

/**
 * Class used to establish connectivity with ZooKeeper server.
 */
public class ZKConnection {
  private ZooKeeper zoo;
  private CountDownLatch connectionLatch = new CountDownLatch(1);

  /**
   * Connect to ZooKeeper server.
   *
   * @param hostList Comma seperated hosts in form of IP_ADDR:PORT.
   * @return ZooKeeper client object.
   */
  public ZooKeeper connect(String hostList, int timeout) throws IOException, InterruptedException {
    zoo = new ZooKeeper(hostList, timeout, new Watcher() {
      public void process(WatchedEvent we) {
        if (we.getState() == KeeperState.SyncConnected) {
          connectionLatch.countDown();
        }
      }
    });
    // Wait until a connection is established
    connectionLatch.await();
    return zoo;
  }

  /**
   * Close connection with ZooKeeper server.
   */
  public void close() throws InterruptedException {
    zoo.close();
  }
}