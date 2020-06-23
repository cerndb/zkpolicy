package ch.cern;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import ch.cern.ZKPolicyDefs.Cli.Query;

@Command(name = "query", aliases = {
    "q" }, description = Query.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKQueryCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKQueryCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Parameters(paramLabel = "[QUERY_NAME]",
      description = Query.QUERY_NAME_DESCRIPTION,
      completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
  String queryName;

  @Option(names = { "-p", "--root-path" }, required = true, description = Query.ROOT_PATH_DESCRIPTION)
  String rootPath;

  @Option(names = { "-a", "--args" }, description = Query.ARGS_DESCRIPTION)
  List<String> queryArgs;

  @Option(names = {"-D", "--description"}, description = Query.DESCR_DESCRIPTION)
  Boolean description = false;

  static class TreeQueryGroup {
    @Option(names = {"--color-description"}, description = Query.COLOR_DESCR_DESCRIPTION)
    Boolean colorDescription = false;
  }

  static class ListQueryGroup {
    @Option(names = { "-l", "--list" }, description = Query.LIST_DESCRIPTION)
    Boolean listMode = false;
  }

  @ArgGroup(exclusive = true, multiplicity = "0..1")
  Exclusive exclusive;

  static class Exclusive {
    @ArgGroup(exclusive = false)
    TreeQueryGroup treeQueryGroup;

    @ArgGroup(exclusive = false)
    ListQueryGroup listQueryGroup;
  }

  @Override
  public void run() {
    try {
      this.executeQuery();
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  private void executeQuery() throws JsonParseException, JsonMappingException, IOException {
    ZKTree zktree = null;

    ZKConfig config = new ZKConfig(parent.configFile);

    try (ZKClient zk = new ZKClient(config)) {
      zktree = new ZKTree(zk);
      StringBuffer outputBuf = new StringBuffer();
      ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();

      // Get query to execute
      ZKQuery query = zkDefaultQuery.getValueOf(this.queryName);

      ZKQueryElement queryElement = new ZKQueryElement(this.queryName, this.rootPath, this.queryArgs, query);
      List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
      queriesList.add(queryElement);

      Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();
      queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());

      // Output query description
      if (this.description) {
        outputBuf.append("Description: "+ queryElement.generateDescription() + "\n\n");
      }

      // Output query result
      if (this.exclusive != null && this.exclusive.listQueryGroup != null && this.exclusive.listQueryGroup.listMode) {
        zktree.queryFind(queryElement.getRootPath(), queriesList, queriesOutput);
      } else {
        zktree.queryTree(queryElement.getRootPath(), queriesList, queriesOutput);
        if (this.exclusive != null && this.exclusive.treeQueryGroup != null && this.exclusive.treeQueryGroup.colorDescription) {
          outputBuf.append(zktree.colorCodeExplanation() + "\n");
        }
      }

      List<String> queryOutput = queriesOutput.get(queryElement.hashCode());
      if (queryOutput.size() > 0) {
        outputBuf.append(String.join("\n", queryOutput));
      }

      System.out.println(outputBuf.toString());
    } catch (NoSuchMethodException | NoSuchFieldException | SecurityException e) {
      System.out.println("No such method: " + this.queryName);
      System.out.println("Please consult the list of default queries using query -h");
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  static class DefaultQueryCandidates extends ArrayList<String> {
    private static final long serialVersionUID = 7171497735085364947L;

    DefaultQueryCandidates() {
      super(Arrays.asList());
      Class<?> zkDefaultQueryClass = ZKDefaultQuery.class;
      Field[] fields = zkDefaultQueryClass.getDeclaredFields();

      for (Field field : fields) {
        if (!field.getName().equals("logger")) {
          this.add("%n * " + field.getName());
        }
      }
      Collections.sort(this);
    }
  }

}
