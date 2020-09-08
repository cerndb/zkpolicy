/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ch.cern.ZKPolicyDefs.Cli.Check;

@Command(name = "check", aliases = {
    "c" }, description = Check.DESCRIPTION, helpCommand = true, mixinStandardHelpOptions = true)
public class ZKCheckCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKCheckCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-p", "--root-path" }, required = true, description = Check.ROOT_PATH_DESCRIPTION)
  String rootPath;

  @Option(names = { "-e",
      "--path-pattern" }, required = true, description = Check.PATH_PATTERN_DESCRIPTION)
  String pathPattern;

  @Option(names = { "-a", "--acls" }, required = true, description = Check.ACLS_DESCRIPTION, arity = "1..*")
  List<String> checkACLs;

  @Option(names = { "-n", "--negate" }, description = Check.NEGATE_DESCRIPTION)
  Boolean negate = false;

  @Option(names = {"-D", "--description"}, description = Check.DESCR_DESCRIPTION)
  Boolean description = false;

  @Override
  public void run() {
    ZKConfig config = null;
    try {
      config = new ZKConfig(parent.configFile);
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }

    if (config != null) {
      try (ZKClient zk = new ZKClient(config)) {
        StringBuffer outputBuf = new StringBuffer();

        ZKCheckElement checkElement = new ZKCheckElement(null, this.rootPath, this.pathPattern, this.negate, this.checkACLs);
        List<ZKCheckElement> checksList = new ArrayList<ZKCheckElement>();
        checksList.add(checkElement);

        // Add check description to output buffer
        if (this.description) {
          outputBuf.append("Description: ");
          outputBuf.append(checkElement.generateDescription() + "\n");
        }

        Hashtable<Integer, List<String>> checksOutput = new Hashtable<Integer, List<String>>();
        checksOutput.put(checkElement.hashCode(), new ArrayList<String>());

        // Execute one check from CLI
        ZKCheck zkCheck = new ZKCheck(zk);
        zkCheck.check(checkElement.getRootPath(), checksList, checksOutput);

        if (checkElement.$status) {
          outputBuf.append(
              "\n" + "Check Result: " + ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMatchColor()).getANSIValue()
                  + "PASS" + ZKPolicyDefs.Colors.RESET.getANSIValue() + "\n");
        } else {
          outputBuf.append(
              "\n" + "Check Result: " + ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMismatchColor()).getANSIValue()
                  + "FAIL" + ZKPolicyDefs.Colors.RESET.getANSIValue() + "\n");
        }
        outputBuf.append("\n" + String.join("\n", checksOutput.get(checkElement.hashCode())) + "\n");
        System.out.println(outputBuf.toString());
      } catch (Exception e) {
        System.out.println(e.toString());
        logger.error("Exception occurred!", e);
      }
    }
  }
}
