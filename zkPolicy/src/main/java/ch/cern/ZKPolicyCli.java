package ch.cern;

import org.apache.commons.cli.*;

/**
 * Class that handles CLI arguments for the tool.
 */
public class ZKPolicyCli {
  public Options optionsMetadata;
  public Options optionsFull;
  private String[] args;

  /**
   * Initialize the cmd line with arguments.
   *
   * @param args Command line arguments as provided at execution by user.
   */
  public ZKPolicyCli(String[] args) {
    this.args = args;
    this.definitionStage();
  }

  /**
   * Definition of available options Two option sets are needed, one for Metadata
   * of the tool (help, usage and version) - options1 and the other for the full
   * set of options.
   */
  private void definitionStage() {

    this.optionsMetadata = new Options();
    Option help = new Option("h", "help", false, "print this message");
    Option version = new Option("v", "version", false, "print the version information and exit");
    optionsMetadata.addOption(help);
    optionsMetadata.addOption(version);

    this.optionsFull = new Options();
    Option config = new Option("c", "config", true, "YAML config file absolute path");
    config.setRequired(true);

    // Option queryMode = new Option("q", "query", true, "fetch ZNode tree according
    // to the passed query ID");
    Option queryMode = Option.builder("q").longOpt("query").argName("query name> <query args")
        .numberOfArgs(Option.UNLIMITED_VALUES).desc("fetch ZNode tree according to the passed query ID")
        .build();

    Option list = new Option("l", "list", false, "print znodes in list format");
    // Add both help and version to be printed in help message
    optionsFull.addOption(help);
    optionsFull.addOption(version);

    // Add the rest cli arguments
    optionsFull.addOption(config);
    optionsFull.addOption(queryMode);
    optionsFull.addOption(list);
  }

  /**
   * CommandLine instance for querying the existence of different options.
   *
   * @param options    The options set that validates the command line arguments
   * @param throwError Enable exception throwing if parsing errors occur
   * @return CommandLine instance for querying the existence of different options
   */
  public CommandLine parseCl(Options options, boolean throwError) throws ParseException {
    CommandLine cl;
    if (throwError) {
      cl = new DefaultParser().parse(options, this.args, true);

    } else {
      cl = new DefaultParser().parse(options, this.args);
    }
    return cl;
  }

}