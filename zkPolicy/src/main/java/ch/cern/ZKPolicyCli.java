package ch.cern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class that handles CLI arguments for the tool
 */
public class ZKPolicyCli {
    private String[] args;
    public Options optionsMetadata;
    public Options optionsFull;

    /**
     *
     * @param args Command line arguments as provided at execution by oser
     */
    public ZKPolicyCli(String[] args) {
        this.args = args;
        this.DefinitionStage();

    }

    /**
     * Definition of available options Two option sets are needed, one for Metadata
     * of the tool (help, usage and version) - options1 and the other for the full
     * set of options.
     */
    private void DefinitionStage() {

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
     *
     * @param options    The options set that validates the command line arguments
     * @param throwError Enable exception throwing if parsing errors occur
     * @return CommandLine instance for querying the existence of different options
     * @throws ParseException
     */
    public CommandLine ParseCl(Options options, boolean throwError) throws ParseException {
        CommandLine cl;
        if (throwError) {
            cl = new DefaultParser().parse(options, this.args, true);

        } else {
            cl = new DefaultParser().parse(options, this.args);
        }
        return cl;
    }

}