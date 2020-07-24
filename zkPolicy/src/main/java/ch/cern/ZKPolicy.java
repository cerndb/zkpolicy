/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import picocli.CommandLine;
import picocli.CommandLine.RunAll;

public final class ZKPolicy {
  private static ZKPolicyCli zkpcli;

  /**
   * Main function of the ZK Policy tool.
   *
   * @param args CLI arguments.
   */
  public static void main(String[] args) {
    zkpcli = new ZKPolicyCli();

    CommandLine commandLine = new CommandLine(zkpcli);

    commandLine.setExecutionStrategy(new RunAll());
    commandLine.execute(args);
  }
}
