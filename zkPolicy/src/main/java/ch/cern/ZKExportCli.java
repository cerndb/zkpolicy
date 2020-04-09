package ch.cern;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.zookeeper.ZooKeeper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "export", aliases = { "e" }, description = "Export the znode tree")
public class ZKExportCli implements Runnable {

    enum Format { json,yaml };

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-t", "--type" }, required = true, description = "Export format ${COMPLETION-CANDIDATES} (default: json)")
    Format format = Format.json;

    @Option(names = { "-C", "--compact" }, description = "Minified export (default: false)")
    Boolean compactMode = false;

    @Option(names = { "-o", "--output" }, required = true, description = "Output file")
    File outputFile = new File("./zkpolicy_export.out");

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to export")
    String rootPath;

    @Override
    public void run() {
        try {
            ZKConnection zkServer = new ZKConnection();
            ZKConfig config = parseConfig(parent.configFile);
            ZooKeeper zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
            ZKTree zktree = new ZKTree(zkClient, config);
            zktree.export(rootPath, format, compactMode, outputFile);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private ZKConfig parseConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        ZKConfig config = om.readValue(configFile, ZKConfig.class);
        config.setPropertyJaas();
        return config;
    }
}