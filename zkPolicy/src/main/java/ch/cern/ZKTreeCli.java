/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
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
import ch.cern.ZKPolicyDefs.Cli.Tree;

@Command(name = "tree", aliases = {
    "t" }, description = Tree.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKTreeCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKTreeCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-p", "--root-path" }, required = true, description = Tree.ROOT_PATH_DESCRIPTION)
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
    public String getDescription(){
      return "Query matching every node of a subtree";
    }
    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      return true;
    }
  }
}
