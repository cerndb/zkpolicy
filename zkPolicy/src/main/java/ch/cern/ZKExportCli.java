package ch.cern;

import java.io.File;
import java.io.IOException;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "export", aliases = { "e" }, description = "Export the znode tree", mixinStandardHelpOptions = true)
public class ZKExportCli implements Runnable {
    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-t",
            "--type" }, required = true, description = "Export format ${COMPLETION-CANDIDATES} (default: json)")
    ZKPolicyDefs.Formats format = ZKPolicyDefs.Formats.json;

    @Option(names = { "-C", "--compact" }, description = "Minified export (default: false)")
    Boolean compactMode = false;

    @Option(names = { "-o", "--output" }, required = true, description = "Output file")
    File outputFile = new File("./zkpolicy_export.out");

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to export")
    String rootPath;

    @Override
    public void run() {
        ZKConfig config = null;
        try {
            config = new ZKConfig(parent.configFile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try (ZKClient zk = new ZKClient(config)){
            ZKExport zkExport = new ZKExport(zk);
            zkExport.export(rootPath, format, compactMode, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}