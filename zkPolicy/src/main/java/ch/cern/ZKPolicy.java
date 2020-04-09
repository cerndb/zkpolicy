package ch.cern;

import org.apache.zookeeper.KeeperException;

import picocli.CommandLine;
import picocli.CommandLine.RunAll;

public final class ZKPolicy {
    private static ZKPolicyCli zkpcli;

    /**
     * Main function of the ZK tool
     *
     * @param args CLI arguments.
     */
    public static void main(String[] args) throws KeeperException, InterruptedException {
        zkpcli = new ZKPolicyCli(args);

        CommandLine commandLine = new CommandLine(zkpcli);

        commandLine.setExecutionStrategy(new RunAll()); // default is RunLast
        commandLine.execute(args);
    }
}
