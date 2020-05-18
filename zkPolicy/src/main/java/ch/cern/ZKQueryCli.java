package ch.cern;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = "query", aliases = {
        "q" }, description = "Query the znode tree", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKQueryCli implements Runnable {
    private static Logger logger = LogManager.getLogger(ZKQueryCli.class);

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
        try {
            this.executeQuery();
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e); 
        }
    }

    private void executeQuery() throws JsonParseException, JsonMappingException, IOException {
        ZKTree zktree = null;

        ZKConfig config = new ZKConfig(parent.configFile);

        try (ZKClient zk = new ZKClient(config)){
            zktree = new ZKTree(zk);

            ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();

            // Get query to execute
            ZKQuery query = zkDefaultQuery.getValueOf(this.queryName);

            ZKQueryElement queryElement = new ZKQueryElement(this.queryName, this.rootPath, this.queryACLs, query);
            List<ZKQueryElement> queriesList = new ArrayList<ZKQueryElement>();
            Hashtable<Integer, List<String>> queriesOutput = new Hashtable<Integer, List<String>>();

            queriesList.add(queryElement);
            queriesOutput.put(queryElement.hashCode(), new ArrayList<String>());
            if (this.listMode) {
                zktree.queryFind(queryElement.getRootpath(), queriesList, queriesOutput);
                System.out.println("\n" + String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
            } else {
                zktree.queryTree(queryElement.getRootpath(), queriesList, queriesOutput);
                System.out.println(zktree.colorCodeExplanation() + String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
            }
        } catch (NoSuchMethodException | SecurityException e) {
            System.out.println("No such method: " + this.queryName);
            System.out.println("Please consult the list of default queries using query -h");
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e); 
        }
    }

    static class DefaultQueryCandidates extends ArrayList<String> {
        DefaultQueryCandidates() {
            super(Arrays.asList());
            Class<?> zkDefaultQueryClass = ZKDefaultQuery.class;
            Field[] fields = zkDefaultQueryClass.getDeclaredFields();

            for (Field field : fields) {
                if (!field.getName().equals("logger")) {
                    this.add("%n * " + field.getName());
                } 
            }
            Collections.sort(this);
        }
    }

}
