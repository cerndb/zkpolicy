/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ch.cern.ZKPolicyDefs.Cli.PurgeRollback;

@Command(name = "purge-rollback", aliases = {
    "p" }, description = PurgeRollback.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKPurgeRollbackCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKPurgeRollbackCli.class);

  @Option(names = { "-r", "--retain-count" }, required = true, description = PurgeRollback.RETAIN_CNT_DESCRIPTION)
  Integer retainCount;

  @Option(names = { "-d",
      "--rollback-dir" }, required = false, defaultValue = PurgeRollback.ROLLBACK_DIR_DEFAULT, description = PurgeRollback.ROLLBACK_DIR_DESCRIPTION)
  File rollbackDir;

  @Override
  public void run() {
    ZKPurgeRollback zkPurge = new ZKPurgeRollback(rollbackDir);
    try {
      zkPurge.purgeRollback(this.retainCount);
    } catch (IOException e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }
}