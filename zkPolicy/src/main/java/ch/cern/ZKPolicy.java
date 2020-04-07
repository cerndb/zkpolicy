package ch.cern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public final class ZKPolicy {

  /**
   * Main function of the ZK tool.
   *
   * @param args CLI arguments.
   */
  public static void main(String[] args) throws KeeperException, InterruptedException {
    // Parse the command line arguments as defined in ZKPolicyCli.DefinitionStage
    ZKPolicyCli zkpcli;
    CommandLine cl;

    zkpcli = new ZKPolicyCli(args);
    try {

      cl = zkpcli.parseCl(zkpcli.optionsMetadata, true);
      // First check for -h, -v using the first options set
      if (cl.getOptions().length != 0) {
        if (cl.hasOption("help")) {
          printHelp(zkpcli);
        } else if (cl.hasOption("version")) {
          System.out.println("zkPolicy v" + getPackageVersion());
        }
      } else {
        cl = zkpcli.parseCl(zkpcli.optionsFull, false);
        if (cl.hasOption("query")) {
          executeQuery(cl);
        }
      }
    } catch (ParseException | IOException e) {
      System.out.println(e.getMessage());
      printHelp(zkpcli);
    }
  }

  private static void executeQuery(CommandLine cl) {
    ZKConnection zkServer;
    ZKTree zktree = null;
    ZooKeeper zkClient;

    zkServer = new ZKConnection();
    ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
    try {
      if (cl.getOptionValue("query").equals("listDefaults")) {
        zkDefaultQuery.listDefaults();
      } else {
        File file = new File(cl.getOptionValue("config"));
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        ZKConfig config = om.readValue(file, ZKConfig.class);
        zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
        zktree = new ZKTree(zkClient, config);
        if (cl.hasOption("list")) {
          System.out.println(zktree.queryFind("/", cl.getOptionValues("query")));
        } else {
          System.out.println(zktree.queryTree("/", cl.getOptionValues("query")));
        }
        zkServer.close();
      }
    } catch (NoSuchMethodException | SecurityException e) {
      System.out.println("No such method: " + cl.getOptionValue("query"));
      System.out.println("Please consult the list of default queries using the [-q listDefaults] parameter");
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | KeeperException
        | InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  private static void printHelp(ZKPolicyCli zkpcli) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("zkpolicy", zkpcli.optionsFull, true);
  }

  private static String getPackageVersion() throws IOException {
    final Properties properties = new Properties();
    properties.load(ZKPolicy.class.getClassLoader().getResourceAsStream("project.properties"));
    return properties.getProperty("version");
  }
}
