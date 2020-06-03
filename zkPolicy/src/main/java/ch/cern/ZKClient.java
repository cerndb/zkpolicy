package ch.cern;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class used to establish connectivity with ZooKeeper server.
 */
public class ZKClient extends ZooKeeper {
  private Logger logger = LogManager.getLogger(ZKClient.class.getName());
  private String host;
  private int port;
  private ZKConfig zkpConfig;

  public String getHost() {
    return this.host;
  }

  /**
   * Get config instance for ZooKeeper Policy Auditing Tool.
   * 
   * @return ZooKeeper Policy Auditing Tool Configuration instance
   */
  public ZKConfig getZKPConfig() {
    return this.zkpConfig;
  }

  public int getPort() {
    return this.port;
  }

  /**
   * Construct ZKClient by providing just a connect string and timeout.
   * 
   * @param connectString  String in format host1:port1,...,hostN:portN
   * @param sessionTimeout Timeout for connecting to ZooKeeper
   * @throws IOException
   */
  public ZKClient(String connectString, int sessionTimeout) throws IOException {
    super(connectString, sessionTimeout, new Watcher() {

      @Override
      public void process(WatchedEvent event) {

      }
    });
    logger.info("Connecting to {} ...", connectString);
    waitUntilConnected(this);
    logger.info("Connection to {} complete", connectString);
  }

  /**
   * Construct ZKClient based on configuration instance.
   * 
   * @param config Configuration instance
   * @throws IOException
   */
  public ZKClient(ZKConfig config) throws IOException {
    super(config.getZkServers(), config.getTimeout(), new Watcher() {

      @Override
      public void process(WatchedEvent event) {
      }
    });
    logger.debug("Connecting to one of {} ...", config.getZkServers());
    waitUntilConnected(this);
    this.zkpConfig = config;
    // Get host and port connected from ZooKeeper tostring
    // Extract host and port
    Pattern hostPortPattern = Pattern.compile("remoteserver:.*?/(.+?):(\\d+)");
    Matcher matches = hostPortPattern.matcher(this.toString());
    if (matches.find()) {
      this.host = matches.group(1);
      this.port = Integer.parseInt(matches.group(2));
    }
    logger.debug("Connection to {} complete", this.host + ":" + this.port);
  }

  private static void waitUntilConnected(ZooKeeper zooKeeper) {
    CountDownLatch connectedLatch = new CountDownLatch(1);
    Watcher watcher = new ZKPolicyUtils.ConnectedWatcher(connectedLatch);
    zooKeeper.register(watcher);
    if (States.CONNECTING == zooKeeper.getState()) {
      try {
        connectedLatch.await();
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }
  }

}
