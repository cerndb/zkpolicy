package ch.cern;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

public class ZKPolicyUtils {
    public static Hashtable<Integer, List<String>> getOutputBuffer(List<ZKQueryElement> queryElements) {
        Hashtable<Integer, List<String>> returnOutput = new Hashtable<Integer, List<String>>();
        for (ZKQueryElement queryElement : queryElements) {    
            // Construct the output buffer HashMap
            returnOutput.put(queryElement.hashCode(), new ArrayList<String>());
        }
        return returnOutput;
    }


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