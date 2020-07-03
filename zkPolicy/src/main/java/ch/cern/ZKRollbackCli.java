package ch.cern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import ch.cern.ZKPolicyDefs.Cli.Rollback;

@Command(name = "rollback", aliases = {
    "r" }, description = Rollback.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKRollbackCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKRollbackCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-i", "--input" }, required = true, description = Rollback.INPUT_STATE_DESCRIPTION)
  File inputStateFile;

  @Override
  public void run() {
    ZKConfig config = null;
    try {
      config = new ZKConfig(parent.configFile);
    } catch (IOException e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }

    if (config != null) {
      try (ZKClient zk = new ZKClient(config)) {
        // check here for SU permissions
        ZKRollbackSet rollbackSet = new ZKRollbackSet(this.inputStateFile);
        rollbackSet.enforceRollback(zk);
      } catch (Exception e) {
        System.out.println(e.toString());
        logger.error("Exception occurred!", e);
      }
    }

  }
}