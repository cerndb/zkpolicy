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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import ch.cern.ZKPolicyDefs.Cli.Export;

@Command(name = "export", aliases = { "e" }, description = Export.DESCRIPTION, mixinStandardHelpOptions = true)
public class ZKExportCli implements Runnable {
  @ParentCommand
  private ZKPolicyCli parent;

  @Option(names = { "-t",
      "--type" }, required = true, description = Export.TYPE_DESCRIPTION)
  ZKPolicyDefs.ExportFormats format = ZKPolicyDefs.ExportFormats.json;

  @Option(names = { "-C", "--compact" }, description = Export.COMPACT_DESCRIPTION)
  Boolean compactMode = false;

  @Option(names = { "-o", "--output" }, required = true, description = Export.OUTPUT_DESCRIPTION)
  File outputFile = new File(Export.OUTPUT_DEFAULT);

  @Option(names = { "-p", "--root-path" }, required = true, description = Export.ROOT_PATH_DESCRIPTION)
  String rootPath;

  @Override
  public void run() {
    ZKConfig config = null;
    try {
      config = new ZKConfig(parent.configFile);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    if (config != null) {
      try (ZKClient zk = new ZKClient(config)) {
        ZKExport zkExport = new ZKExport(zk);
        zkExport.export(rootPath, format, compactMode, outputFile);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }
}