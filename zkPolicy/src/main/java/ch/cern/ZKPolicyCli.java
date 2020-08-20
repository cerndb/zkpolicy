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
import java.util.Properties;
import lombok.NoArgsConstructor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import ch.cern.ZKPolicyDefs.Cli.ZkPolicy;


@Command(name = "zkpolicy",
    description = ZkPolicy.DESCRIPTION,
    versionProvider = ZKPolicyCli.PropertiesVersionProvider.class,
    subcommands = {
        ZKQueryCli.class,
        ZKExportCli.class,
        ZKTreeCli.class,
        ZKEnforceCli.class,
        ZKAuditCli.class,
        ZKCheckCli.class,
        ZKRollbackCli.class,
        HelpCommand.class
      },
    mixinStandardHelpOptions = true)
/**
 * Class that handles CLI arguments for the tool.
 */
@NoArgsConstructor
public class ZKPolicyCli implements Runnable {
  @Option(names = { "-c",
      "--config" }, required = false,
      description = ZkPolicy.CONFIG_DESCRIPTION,
      scope = ScopeType.INHERIT,
      defaultValue = ZkPolicy.CONFIG_DEFAULT)
  public File configFile;

  @Option(names = { "-j",
      "--jaas" }, required = false,
      description = ZkPolicy.JAAS_FILE_DESCRIPTION,
      scope = ScopeType.INHERIT)
  public File jaasFile;

  @Spec
  CommandSpec spec;

  @Override
  public void run() {
    // if the command was invoked without subcommand, show the usage help
    spec.commandLine().usage(System.err);
  }

  public int executionStrategy(ParseResult parseResult) {
    init(); // custom initialization to be done before executing any command or subcommand
    return new CommandLine.RunLast().execute(parseResult); // default execution strategy
  }

  // Initialize state for parent command here
  private void init() {
    if (this.jaasFile != null && this.jaasFile.exists()) {
      System.setProperty("java.security.auth.login.config", this.jaasFile.getAbsolutePath());
    }
  }

  static class PropertiesVersionProvider implements IVersionProvider {
    public String[] getVersion() throws Exception {
      final Properties properties = new Properties();
      properties.load(ZKPolicy.class.getClassLoader().getResourceAsStream("project.properties"));
      return new String[] { properties.getProperty("version") };
    }
  }
}