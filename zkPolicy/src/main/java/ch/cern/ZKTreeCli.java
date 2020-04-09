package ch.cern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "tree", aliases = { "t" }, description = "tree command for ZooKeeper", helpCommand = true)
public class ZKTreeCli implements Runnable {

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to execute query")
    String rootPath;

    @Override
    public void run() {
        this.tree();
    }

    private void tree() {
        ZKConnection zkServer;
        ZooKeeper zk;
        List<String> output = new ArrayList<String>();

        zkServer = new ZKConnection();
        try {
            ZKConfig config = parseConfig(parent.configFile);
            zk = zkServer.connect(config.getZkservers(), config.getTimeout());
            treeRecursive(output, zk, this.rootPath, "", "", true, false, false);
            System.out.println(String.join("\n", output));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void treeRecursive(List<String> output, ZooKeeper zk, String path, String indent, String name,
            boolean isQueryRoot, boolean isLast, boolean isParentLast) {
        List<String> children = null;
        try {
            children = zk.getChildren(path, null);
        } catch (KeeperException | InterruptedException e) {
            return;
        }

        if (path.equals("/")) {
            path = "";
        } else if (isQueryRoot) {
            name = path.substring(1, path.length());
        } else {
            if (name.equals("")) {
                name = path.substring(1, path.length());
            }
            if (indent.length() > 0) {
                if (isParentLast) {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.lastParentIndent;
                } else {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.innerParentIndent;
                }
            }
            if (isLast) {
                indent += ZKPolicyDefs.TerminalConstants.lastChildIndent;
            } else {
                indent += ZKPolicyDefs.TerminalConstants.innerChildIndent;
            }

        }
        output.add(indent + "/" + name);

        Collections.sort(children);
        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            this.treeRecursive(output, zk, path + "/" + child, indent, child, false, !iterator.hasNext(), isLast);
        }
    }

    private ZKConfig parseConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        ZKConfig config = om.readValue(configFile, ZKConfig.class);
        config.setPropertyJaas();
        config.setPropertyLog4j();
        return config;
    }
}
