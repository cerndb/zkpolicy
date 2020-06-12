package ch.cern;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class ZKPolicyUtils {
  /**
   * Construct output buffer for a list of query elements.
   * 
   * @param queryElements Queries to be executed
   * @return Output buffer
   */
  public static Hashtable<Integer, List<String>> getQueryOutputBuffer(List<ZKQueryElement> queryElements) {
    Hashtable<Integer, List<String>> returnOutput = new Hashtable<Integer, List<String>>();
    for (ZKQueryElement queryElement : queryElements) {
      // Construct the output buffer HashMap
      returnOutput.put(queryElement.hashCode(), new ArrayList<String>());
    }
    return returnOutput;
  }

  /**
   * Construct output buffer for a list of check elements.
   * 
   * @param checkElements Checks to be executed
   * @return Output buffer
   */
  public static Hashtable<Integer, List<String>> getChecksOutputBuffer(List<ZKCheckElement> checkElements) {
    Hashtable<Integer, List<String>> returnOutput = new Hashtable<Integer, List<String>>();
    for (ZKCheckElement checkElement : checkElements) {
      // Construct the output buffer HashMap
      returnOutput.put(checkElement.hashCode(), new ArrayList<String>());
    }
    return returnOutput;
  }

  /**
   * Check if address is subnet range.
   * 
   * @param address Address string to be checked
   * @return
   */
  public static Boolean isIPV4SubnetAddress(String address) {
    String owaspPattern = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\/(3[0-2]|2[0-9]|1[0-9]|[0-9])$";
    Pattern subnetRegex = Pattern.compile(owaspPattern);
    Matcher patternMatcher = subnetRegex.matcher(address);
    return patternMatcher.matches();
  }

  /**
   * Validate IPv4 address.
   * @param address Address string to be checked
   * @return
   */
  public static Boolean isIPV4Address(String address) {
    String owaspPattern = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
        + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    Pattern subnetRegex = Pattern.compile(owaspPattern);
    Matcher patternMatcher = subnetRegex.matcher(address);
    return patternMatcher.matches();
  }

  /**
   * Watcher for ZooKeeper client connection that blocks waiting for succesful
   * connection.
   */
  public static class ConnectedWatcher implements Watcher {
    private CountDownLatch connectedLatch;

    ConnectedWatcher(CountDownLatch connectedLatch) {
      this.connectedLatch = connectedLatch;
    }

    @Override
    public void process(WatchedEvent event) {
      if (event.getState() == KeeperState.SyncConnected) {
        connectedLatch.countDown();
      }
    }
  }
}