package ch.cern;

import java.io.File;
import java.util.Properties;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;

@Command(name = "zkpolicy", description = "ZooKeeper policy auditing tool", versionProvider = ZKPolicyCli.PropertiesVersionProvider.class, subcommands = {
        ZKQueryCli.class, ZKExportCli.class, ZKTreeCli.class, HelpCommand.class }, mixinStandardHelpOptions = true)
/**
 * Class that handles CLI arguments for the tool.
 */
public class ZKPolicyCli implements Runnable {
    String[] args;

    public ZKPolicyCli(String[] args) {
        this.args = args;
    }

    @Option(names = { "-c", "--config" }, description = "YAML config file absolute path")
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
