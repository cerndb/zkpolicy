/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
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
import ch.cern.ZKPolicyDefs.Cli.Audit;

@Command(name = "audit", aliases = { "a" }, description = Audit.DESCRIPTION, mixinStandardHelpOptions = true)
public class ZKAuditCli implements Runnable {
  private static Logger logger = LogManager.getLogger(ZKAuditCli.class);

  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-o", "--output" }, required = false, description = Audit.OUTPUT_DESCRIPTION)
  File outputFile = null;

  @Option(names = { "-i",
      "--input" }, required = false,
      description = Audit.INPUT_DESCRIPTION, defaultValue = Audit.INPUT_DEFAULT)
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

        ZKAuditSet.ZKPolicyReportSections sections = zkAudit.getZkAuditSet().getSections();

        if (sections.isGeneralInformation()) {
          outputString += this.addZKHeader();
          outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;
        }

        if (sections.isFourLetterWordCommands()) {
          outputString += zkAudit.getFourLetterWordOverview();
          outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;
        }

        if (sections.isQueryResults()) {
          outputString += zkAudit.generateQueriesSection();
          outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;
        }

        if (sections.isCheckResults()) {
          outputString += zkAudit.generateChecksSection();
          outputString += ZKPolicyDefs.TerminalConstants.sectionSeparator;
        }

        if (sections.isAclOverview()) {
          outputString += zkAudit.getACLOverview();
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
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
      System.out.println(e.getMessage());
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