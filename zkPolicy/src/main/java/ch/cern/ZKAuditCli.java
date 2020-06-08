package ch.cern;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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

  @Option(names = { "-i",
      "--input" }, required = false,
      description = "Audit report configuration file (default: ${DEFAULT-VALUE})", defaultValue = "/opt/zkpolicy/conf/audit.yml")
  File auditConfigFile;

  ZKConfig config;
  ZKClient zk;

  @Override
  public void run() {
    OutputStreamWriter writer = null;
    String outputString = "";
    try {
      ZKAudit zkAudit = null;

      config = new ZKConfig(parent.configFile);

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

      } catch (Exception e) {
        System.out.println(e.toString());
        logger.error("Exception occurred!", e);
      }

      OutputStream writerStream;
      // Print in output file or stdout
      if (outputFile != null) {
        writerStream = new FileOutputStream(this.outputFile);
      } else {
        writerStream = System.out;
      }
      writer = new OutputStreamWriter(writerStream, "UTF-8");
      writer.write(outputString);
      writer.flush();
      writer.close();
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

    headerOut += "Report results for file: " + auditConfigFile.getCanonicalPath() + "\n";

    headerOut += "Connected to ZooKeeper server: " + this.zk.getHost() + ":" + this.zk.getPort() + "\n";
    return headerOut;
  }
}