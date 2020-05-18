package ch.cern;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;


@Command(name = "audit", aliases = { "a" }, description = "Generate full audit report", mixinStandardHelpOptions = true)
public class ZKAuditCli implements Runnable {
    private static Logger logger = LogManager.getLogger(ZKAuditCli.class);

    @ParentCommand
    private ZKPolicyCli parent;
    
    @Option(names = { "-o", "--output" }, required = false, description = "Audit report output file")
    File outputFile = null;
    
    @Option(names = { "-i", "--input" }, required = false, description = "Audit report configuration file")
    File auditConfigFile = null;

    ZKConfig config;
    ZKClient zk;

    @Override
    public void run() {
        try {
            BufferedWriter writer = null;
            String outputString = "";
            
            ZKAudit zkAudit = null;

            config = new ZKConfig(parent.configFile);

            // Define the query set to be executed for each node as well as the output arrays for each query
            // Check the passed audit yaml config file for queries and construct the HashTable
            if (this.auditConfigFile == null) {
                this.auditConfigFile = new File(config.getDefaultauditpath());
            } 
            
            try (ZKClient zk = new ZKClient(config)) {
                this.zk = zk;
                zkAudit = new ZKAudit(zk, this.auditConfigFile);

                // First construct the header
                outputString += this.addZKHeader();
                outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;

                outputString += zkAudit.getFourLetterWordOverview();
                outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;                
                
                outputString += zkAudit.generateHumanReadableAuditReport();
                outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;

                // Then get ACL Overview in footer 
                outputString += zkAudit.getACLOverview();
                
            } catch ( Exception e ) {
                System.out.println(e.toString()); 
                logger.error("Exception occurred!", e);
            }

            
            // Print in output file or stdout
            if(outputFile != null){
                writer = new BufferedWriter(new FileWriter(this.outputFile));    
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(System.out));
            }
            writer.write(outputString);
            writer.flush();
            writer.close();
            
            // Decide whether this would be human readable or for enforcing later on
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }

    }

    private String addZKHeader() throws Exception {
        String headerOut = "";
        // Get ZKPolicy Version
        ZKPolicyCli.PropertiesVersionProvider versionProvider = new ZKPolicyCli.PropertiesVersionProvider();
        headerOut += "ZKPolicy v" + versionProvider.getVersion()[0] + " Audit report\n";
        
        // Get current datetime
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z", Locale.getDefault());
        Date date = new Date();
        headerOut += "DateTime: " + dateFormat.format(date) + "\n";
        if (auditConfigFile == null) {
            headerOut += "Report results for file: " + new File(this.config.getDefaultauditpath()).getCanonicalPath() + "\n";   
        } else {
            headerOut += "Report results for file: " + auditConfigFile.getCanonicalPath() + "\n";
        }

        headerOut += "Connected to ZooKeeper server: " + this.zk.getHost() + ":" + this.zk.getPort() + "\n";
        return headerOut;
    }
}