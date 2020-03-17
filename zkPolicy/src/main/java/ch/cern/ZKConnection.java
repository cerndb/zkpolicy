package ch.cern;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;


/**
 * Class used to establish connectivity with ZooKeeper server
 */
public class ZKConnection {
    private ZooKeeper zoo;
    private CountDownLatch connectionLatch = new CountDownLatch(1);
 
    /**
     * Connect to ZooKeeper server
     * @param host Host in form of IP_ADDR:PORT
     * @return ZooKeeper client object 
     * @throws IOException
     * @throws InterruptedException
     */
    public ZooKeeper connect(String host) 
      throws IOException, 
      InterruptedException {
        zoo = new ZooKeeper(host, 2000, new Watcher() {
            public void process(WatchedEvent we) {
                if (we.getState() == KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            }
        });
 
        connectionLatch.await();
        return zoo;
    }
 
    /**
     * Close connection with ZooKeeper server
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        zoo.close();
    }
}