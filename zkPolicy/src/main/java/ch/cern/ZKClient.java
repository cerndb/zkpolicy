package ch.cern;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used to establish connectivity with ZooKeeper server.
 */
public class ZKClient extends ZooKeeper {
    private Logger logger = LoggerFactory.getLogger(ZKClient.class.getName());
    private String host;
    private int port;
    private ZKConfig zkpConfig;

    public String getHost(){
        return this.host;
    }

    public ZKConfig getZKPConfig(){
        return this.zkpConfig;
    }

    public int getPort(){
        return this.port;
    }

    public ZKClient(String connectString, int sessionTimeout) throws IOException {
        super(connectString, sessionTimeout, null);
        waitUntilConnected(this);
    }

    public ZKClient(ZKConfig config) throws IOException {
        super(config.getZkservers(), config.getTimeout(), null);
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
    }

    public static void waitUntilConnected(ZooKeeper zooKeeper) {
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
