package ch.cern;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.zookeeper.data.ACL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "tree", aliases = {
    "t" }, description = "tree command for ZooKeeper", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKTreeCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKTreeCli.class);

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
    ZKQuery query = new TreeAlwaysTrueQuery();
    ZKQueryElement queryElement = new ZKQueryElement("treeAlwaysTrueQuery", this.rootPath, null, query);

    queriesList.add(queryElement);
    queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

    ZKConfig config = null;
    try {
      config = new ZKConfig(parent.configFile);
      config.setMatchColor("WHITE");
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }

    if (config != null) {
      try (ZKClient zk = new ZKClient(config)) {
        zktree = new ZKTree(zk);
        zktree.queryTree(queryElement.getRootPath(), queriesList, queriesOutput);
      } catch (Exception e) {
        System.out.println(e.toString());
        logger.error("Exception occurred!", e);
      }
      System.out.println(String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
    }
  }

  private static class TreeAlwaysTrueQuery implements ZKQuery {
    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      return true;
    }
  }
}
