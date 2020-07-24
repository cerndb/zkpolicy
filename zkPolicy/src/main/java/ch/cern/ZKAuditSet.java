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
import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * Class that holds the report scenario description with different steps
 * (queries and checks) to be executed on the ZK tree argument.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKAuditSet {

  private List<ZKQueryElement> queries;
  private List<ZKCheckElement> checks;
  private ZKPolicyReportSections sections;

  public ZKAuditSet(File auditConfigFile) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    om.readerForUpdating(this).readValue(auditConfigFile);
  }

  /**
   * Structure of report that defines which sections to be included.
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  public static class ZKPolicyReportSections {
    private boolean generalInformation;
    private boolean fourLetterWordCommands;
    private boolean queryResults;
    private boolean checkResults;
    private boolean aclOverview;
  }
}