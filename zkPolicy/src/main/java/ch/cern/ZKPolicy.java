package ch.cern;

import org.apache.zookeeper.KeeperException;

import picocli.CommandLine;
import picocli.CommandLine.RunAll;

public final class ZKPolicy {
    private static ZKPolicyCli zkpcli;

    /**
     * Main function of the ZK Policy tool
     *
     * @param args CLI arguments.
     * @exception KeeperException
     * @exception InterruptedException
     */
    public static void main(String[] args) throws KeeperException, InterruptedException {
        zkpcli = new ZKPolicyCli(args);

        CommandLine commandLine = new CommandLine(zkpcli);

        commandLine.setExecutionStrategy(new RunAll());
        commandLine.execute(args);
    }
}
