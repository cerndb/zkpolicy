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
