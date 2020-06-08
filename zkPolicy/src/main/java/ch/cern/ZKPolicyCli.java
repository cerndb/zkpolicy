package ch.cern;

import java.io.File;
import java.util.Properties;
import lombok.NoArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

@Command(name = "zkpolicy",
    description = "ZooKeeper policy auditing tool",
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
      description = "YAML configuration file to use (default: ${DEFAULT-VALUE})",
      scope = ScopeType.INHERIT,
      defaultValue = "/opt/zkpolicy/conf/config.yml")
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