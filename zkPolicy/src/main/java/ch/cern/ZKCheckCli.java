package ch.cern;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

@Command(name = "check", aliases = {
        "c" }, description = "Check specific znodes for ACL match", helpCommand = true, mixinStandardHelpOptions = true)
public class ZKCheckCli implements Runnable {
    private static Logger logger = LogManager.getLogger(ZKCheckCli.class);

    @ParentCommand
    private ZKPolicyCli parent;

    @Option(names = { "-p", "--root-path" }, required = true, description = "Root path to execute query")
    String rootPath;

    @Option(names = { "-e", "--path-pattern" }, required = true, description = "Path pattern that must be satisfied to check node")
    String pathPattern;

    @Option(names = { "-a", "--acls" }, description = "ACLs for checking against matching znodes")
    String[] checkACLs;

    @Override
    public void run() {
        ZKConfig config = null;
        try {
            config = new ZKConfig(parent.configFile);
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }

        try (ZKClient zk = new ZKClient(config)){
            // Execute one check from CLI
            ZKCheck zkCheck = new ZKCheck(zk);
            
            ZKCheckElement checkElement = new ZKCheckElement(null, this.rootPath, this.pathPattern, this.checkACLs);
            List<ZKCheckElement> checksList = new ArrayList<ZKCheckElement>();
            Hashtable<Integer, List<String>> checksOutput = new Hashtable<Integer, List<String>>();

            checksList.add(checkElement);
            checksOutput.put(checkElement.hashCode(), new ArrayList<String>());

            zkCheck.check(checkElement.getRootPath(), checksList, checksOutput);

            if (checkElement.$status) {
                System.out.println("\n" + "Check Result: " + ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMatchColor()).getANSIValue() + "PASS" + ZKPolicyDefs.Colors.RESET.getANSIValue());
            } else {
                System.out.println("\n" + "Check Result: "+ ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMismatchColor()).getANSIValue() +"FAIL" + ZKPolicyDefs.Colors.RESET.getANSIValue());
            }
            System.out.println("\n" + String.join("\n", checksOutput.get(checkElement.hashCode())) + "\n");

        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e); 
        }
    }
}
