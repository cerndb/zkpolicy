package ch.cern;

import java.io.File;
import java.io.IOException;
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

@Command(name = "enforce", aliases = {
    "f" }, description = "Enforce policy on znodes", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKEnforceCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKEnforceCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  static class FileEnforceGroup {
    @Option(names = { "-i", "--input" }, required = true, description = "File with policy definitions to enforce")
    File policiesFile;
  }

  static class CliEnforceGroup {
    @Option(names = { "-P", "--policy" }, required = true, description = "Policies to enforce on matching nodes")
    String[] policies;

    @Parameters(paramLabel = "[QUERY_NAME]",
        description = "Query to be executed: ${COMPLETION-CANDIDATES}",
        completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
    String queryName;

    @Option(names = { "-p", "--root-path" }, required = false, description = "Path pattern to match")
    String rootPath = null;

    @Option(names = { "-a", "--args" }, required = false, description = "Query arguments")
    String[] queryArgs;

    @Option(names = { "-A",
        "--append" }, required = false, description = "Append policy ACLs to znode's ACL (default: false)")
    boolean append = false;
  }

  @ArgGroup(exclusive = true, multiplicity = "1")
  Exclusive exclusive;

  @Option(names = { "-d", "--dry-run" }, required = false, description = "Dry run execution")
  boolean dryRun = false;

  static class Exclusive {
    @ArgGroup(exclusive = false)
    FileEnforceGroup fileEnforceGroup;

    @ArgGroup(exclusive = false)
    CliEnforceGroup cliEnforceGroup;

  }

  @Override
  public void run() {
    try {
      if (this.exclusive.fileEnforceGroup == null) {
        this.cliEnforce();
      } else if (this.exclusive.cliEnforceGroup == null) {
        this.cliEnforceFromFIle();
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  /**
   * Enforce policy passed through CLI when using the --policy flag.
   * 
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  private void cliEnforce() throws JsonParseException, JsonMappingException, IOException {
    ZKConfig config = new ZKConfig(parent.configFile);

    try (ZKClient zk = new ZKClient(config)) {
      ZKEnforce zkEnforce = new ZKEnforce(zk);

      if (this.dryRun) {
        zkEnforce.enforceDry(this.exclusive.cliEnforceGroup.queryName, this.exclusive.cliEnforceGroup.rootPath,
            this.exclusive.cliEnforceGroup.queryArgs);
      } else {
        zkEnforce.enforce(this.exclusive.cliEnforceGroup.policies, this.exclusive.cliEnforceGroup.queryName,
            this.exclusive.cliEnforceGroup.rootPath, this.exclusive.cliEnforceGroup.queryArgs,
            this.exclusive.cliEnforceGroup.append);
      }
    } catch (NoSuchMethodException | NoSuchFieldException e) {
      System.out.println("No such method: " + this.exclusive.cliEnforceGroup.queryName);
      System.out.println("Please consult the list of default queries using query -h");
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  /**
   * Enforce policies defined in --input option file path.
   * 
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  private void cliEnforceFromFIle() throws JsonParseException, JsonMappingException, IOException {
    ZKConfig config = new ZKConfig(parent.configFile);

    try (ZKClient zk = new ZKClient(config)) {
      ZKEnforce zkEnforce = new ZKEnforce(zk);
      ZKEnforcePolicySet policySet = new ZKEnforcePolicySet(this.exclusive.fileEnforceGroup.policiesFile);
      List<ZKEnforcePolicyElement> policies = policySet.getPolicies();

      // For each of the policies, execute enforce passing each of the parameters
      for (ZKEnforcePolicyElement policy : policies) {

        if (this.dryRun) {
          System.out.println(policy.getTitle());
          zkEnforce.enforceDry(policy.getQuery().getName(), policy.getQuery().getRootPath(),
              policy.getQuery().getArgs());
          System.out.print("\n");

        } else {
          zkEnforce.enforce(policy.getAcls(), policy.getQuery().getName(), policy.getQuery().getRootPath(),
              policy.getQuery().getArgs(), policy.isAppend());
        }
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }
}