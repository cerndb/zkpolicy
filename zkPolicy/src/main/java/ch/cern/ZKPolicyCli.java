package ch.cern;

import java.io.File;
import java.util.Properties;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
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

  @Override
  public void run() {
  }

  static class PropertiesVersionProvider implements IVersionProvider {
    public String[] getVersion() throws Exception {
      final Properties properties = new Properties();
      properties.load(ZKPolicy.class.getClassLoader().getResourceAsStream("project.properties"));
      return new String[] { properties.getProperty("version") };
    }
  }
}