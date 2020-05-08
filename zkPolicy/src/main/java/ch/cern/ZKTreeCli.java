package ch.cern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.zookeeper.data.ACL;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "tree", aliases = {
        "t" }, description = "tree command for ZooKeeper", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKTreeCli implements Runnable {

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to execute query")
    String rootPath;

    @Override
    public void run() {
        ZKTree zktree = null;

        Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
        List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();

        // Add the treeAlwaysTrueQuery query to the execution list
        ZKQuery query = new treeAlwaysTrueQuery();
        ZKQueryElement queryElement = new ZKQueryElement("treeAlwaysTrueQuery", this.rootPath, null, query);

        queriesList.add(queryElement);
        queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

        ZKConfig config = null;
        try {
            config = new ZKConfig(parent.configFile);
            config.setMatchcolor("WHITE");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try (ZKClient zk = new ZKClient(config)){
            zktree = new ZKTree(zk);
            zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
    }

    private class treeAlwaysTrueQuery implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZKClient zk, String[] queryACLs){
            return true;
        }
    }
}
