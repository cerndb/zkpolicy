/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import ch.cern.ZKPolicyDefs.Cli.Enforce;

@Command(name = "enforce", aliases = {
    "f" }, description = Enforce.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKEnforceCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKEnforceCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-r", "--rollback-export" }, required = false, description = Enforce.ROLLBACK_EXPORT_DESCRIPTION)
  File rollbackStateFile;

  static class FileEnforceGroup {
    @Option(names = { "-i",
        "--input" }, required = true, description = Enforce.INPUT_DESCRIPTION, defaultValue = Enforce.INPUT_DEFAULT)
    File policiesFile;
  }

  static class ServiceEnforceGroup {
    @Option(names = { "-s",
        "--service-policy" }, required = true, description = Enforce.SERVICE_POLICY_DESCRIPTION,
        completionCandidates = ZKEnforceCli.DefaultQueryCandidates.class, arity = "1..*")
        List<String> services;
    @Option(names = { "-D",
        "--service-policies-dir" }, required = false, description = Enforce.SERVICE_POLICIES_DIR_DESCRIPTION,
        defaultValue = Enforce.SERVICE_POLICIES_DIR_DEFAULT)
        File servicePoliciesDir;
  }

  static class CliEnforceGroup {
    @Option(names = { "-P", "--policy" }, required = true, description = Enforce.POLICY_DESCRIPTION)
    List<String> policies;

    @Parameters(paramLabel = "[QUERY_NAME]", description = Enforce.QUERY_NAME_DESCRIPTION,
        completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
    String queryName;

    @Option(names = { "-p", "--root-path" }, required = false, description = Enforce.ROOT_PATH_DESCRIPTION)
    String rootPath;

    @Option(names = { "-a", "--args" }, required = false, description = Enforce.ARGS_DESCRIPTION)
    List<String> queryArgs;

    @Option(names = { "-A", "--append" }, required = false, description = Enforce.APPEND_DESCRIPTION)
    boolean append = false;
  }

  @Option(names = { "-d", "--dry-run" }, required = false, description = Enforce.DRY_RUN_DESCRIPTION)
  boolean dryRun = false;

  @ArgGroup(exclusive = true, multiplicity = "1")
  Exclusive exclusive;

  static class Exclusive {
    @ArgGroup(exclusive = false)
    FileEnforceGroup fileEnforceGroup;

    @ArgGroup(exclusive = false)
    CliEnforceGroup cliEnforceGroup;

    @ArgGroup(exclusive = false)
    ServiceEnforceGroup serviceEnforceGroup;
  }

  @Override
  public void run() {
    try {
      if (this.exclusive.fileEnforceGroup == null && this.exclusive.serviceEnforceGroup == null) {
        this.cliEnforce();
      } else if (this.exclusive.cliEnforceGroup == null && this.exclusive.serviceEnforceGroup == null) {
        this.cliEnforceFromFile(this.exclusive.fileEnforceGroup.policiesFile);
      } else if (this.exclusive.cliEnforceGroup == null && this.exclusive.fileEnforceGroup == null) {
        // Multiple service policies can be enforced with a single enforce execution
        for (String servicePolicy : this.exclusive.serviceEnforceGroup.services) {
          File policiesFile = new File(this.exclusive.serviceEnforceGroup.servicePoliciesDir.toString() + "/" + servicePolicy + ".yml");
          this.cliEnforceFromFile(policiesFile);
        }
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
      // Construct query
      ZKQueryElement query = new ZKQueryElement();
      query.setName(this.exclusive.cliEnforceGroup.queryName);
      query.setArgs(this.exclusive.cliEnforceGroup.queryArgs);
      query.setRootPath(this.exclusive.cliEnforceGroup.rootPath);

      // Construct policy
      ZKEnforcePolicyElement policy = new ZKEnforcePolicyElement();
      policy.setQuery(query);
      policy.setAppend(this.exclusive.cliEnforceGroup.append);
      policy.setAcls(this.exclusive.cliEnforceGroup.policies);

      if (this.dryRun) {
        ZKEnforce zkEnforce = new ZKEnforce(zk);
        zkEnforce.enforceDry(policy);
      } else {
        // check whether rollback file is defined
        if (this.rollbackStateFile == null) {
          DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
          LocalDateTime now = LocalDateTime.now();
          this.rollbackStateFile = new File("/opt/zkpolicy/rollback/ROLLBACK_STATE_" + dtf.format(now) + ".yml");
        }
        ZKEnforce zkEnforce = new ZKEnforce(zk, this.rollbackStateFile);
        zkEnforce.enforce(policy);
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
   * Enforce policies defined policies file.
   * 
   * @param policiesFile Policy definition file
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  private void cliEnforceFromFile(File policiesFile) throws JsonParseException, JsonMappingException, IOException {
    ZKConfig config = new ZKConfig(parent.configFile);

    try (ZKClient zk = new ZKClient(config)) {
      ZKEnforcePolicySet policySet = new ZKEnforcePolicySet(policiesFile);
      List<ZKEnforcePolicyElement> policies = policySet.getPolicies();

      // For each of the policies, execute enforce passing each of the parameters
      for (ZKEnforcePolicyElement policy : policies) {

        if (this.dryRun) {
          ZKEnforce zkEnforce = new ZKEnforce(zk);
          System.out.println(policy.getTitle());
          zkEnforce.enforceDry(policy);
          System.out.print("\n");

        } else {
          // check whether rollback file is defined
          if (this.rollbackStateFile == null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            this.rollbackStateFile = new File("/opt/zkpolicy/rollback/ROLLBACK_STATE_" + dtf.format(now) + ".yml");
          }
          ZKEnforce zkEnforce = new ZKEnforce(zk, this.rollbackStateFile);
          zkEnforce.enforce(policy);
        }
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  static class DefaultQueryCandidates extends ArrayList<String> {
    private static final long serialVersionUID = 1L;

    DefaultQueryCandidates() {
      super(Arrays.asList());

      this.add("%n * kafka");
      this.add("%n * hbase");
      this.add("%n * hive");
      this.add("%n * ooozie");
      this.add("%n * yarn");
      this.add("%n * hdfs");

      Collections.sort(this);
    }
  }
}