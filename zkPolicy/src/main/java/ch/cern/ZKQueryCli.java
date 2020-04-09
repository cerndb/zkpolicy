package ch.cern;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.zookeeper.ZooKeeper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "query", aliases = { "q" }, description = "Query the znode tree", helpCommand = true)
public class ZKQueryCli implements Runnable {

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-l", "--list" }, description = "Enable list mode (default: disabled)")
    Boolean listMode = false;

    @Parameters(paramLabel = "[QUERY_NAME]", description = "Query to be executed: ${COMPLETION-CANDIDATES}", completionCandidates = ZKQueryCli.DefaultQueryCandidates.class)
    String queryName;

    @Option(names = { "-p", "--path" }, required = true, description = "Root path to execute query")
    String rootPath;

    @Option(names = { "-a", "--acls" }, description = "ACLs for querying")
    String[] queryACLs;

    @Override
    public void run() {
        this.executeQuery();
    }

    private void executeQuery() {
        ZKConnection zkServer;
        ZKTree zktree = null;
        ZooKeeper zkClient;

        zkServer = new ZKConnection();
        try {

            ZKConfig config = parseConfig(parent.configFile);
            zkClient = zkServer.connect(config.getZkservers(), config.getTimeout());
            zktree = new ZKTree(zkClient, config);
            if (this.listMode) {
                System.out.println(zktree.queryFind(this.queryName, this.rootPath, this.queryACLs));
            } else {
                System.out.println(zktree.queryTree(this.queryName, this.rootPath, this.queryACLs));
            }
            zkServer.close();

        } catch (NoSuchMethodException | SecurityException e) {
            System.out.println("No such method: " + this.queryName);
            System.out.println("Please consult the list of default queries using the [-q listDefaults] parameter");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ZKConfig parseConfig(File configFile) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        ZKConfig config = om.readValue(configFile, ZKConfig.class);
        config.setPropertyJaas();
        config.setPropertyLog4j();
        return config;
    }

    static class DefaultQueryCandidates extends ArrayList<String> {
        DefaultQueryCandidates() {
            super(Arrays.asList());
            Class<?> zkDefaultQueryClass = ZKDefaultQuery.class;
            Method[] methods = zkDefaultQueryClass.getDeclaredMethods();

            for (Method method : methods) {
                String methodName = method.getName();
                if (!methodName.startsWith("lambda$")) {
                    this.add("%n * " + methodName);
                }
            }
            Collections.sort(this);
        }
    }

}
