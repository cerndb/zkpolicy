package ch.cern;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.zookeeper.ZooKeeper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "query", aliases = { "q" }, description = "Query the znode tree", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKQueryCli implements Runnable {

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-l", "--list" }, description = "Enable list mode (default: disabled)")
    Boolean listMode = false;

    @Parameters(paramLabel = "[QUERY_NAME]", description = "Query to be executed: ${COMPLETION-CANDIDATES}", completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
    String queryName;

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to execute query")
    String rootPath;

    @Option(names = { "-a", "--acls" }, description = "ACLs for querying")
    String[] queryACLs;

    @Override
    public void run() {
        this.executeQuery();
    }

    private void executeQuery() {
        ZKTree zktree = null;
        ZooKeeper zkClient;

        try (ZKConnection zkServer = new ZKConnection()){
            ZKConfig config = new ZKConfig(parent.configFile);
            zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
            zktree = new ZKTree(zkClient, config);
            if (this.listMode) {
                System.out.println(zktree.queryFind(this.queryName, this.rootPath, this.queryACLs));
            } else {
                System.out.println(zktree.queryTree(this.queryName, this.rootPath, this.queryACLs));
            }
            zkServer.close();

        } catch (NoSuchMethodException | SecurityException e) {
            System.out.println("No such method: " + this.queryName);
            System.out.println("Please consult the list of default queries using query -h");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class DefaultQueryCandidates extends ArrayList<String> {
        DefaultQueryCandidates() {
            super(Arrays.asList());
            Class<?> zkDefaultQueryClass = ZKDefaultQuery.class;
            Field[] fields = zkDefaultQueryClass.getDeclaredFields();

            for (Field field : fields) {
                this.add("%n * " + field.getName());
            }
            Collections.sort(this);
        }
    }

}
