/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Class for query elements to be executed, constructed based on configuration
 * yaml files argument.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKQueryElement {

  private String name;
  private String rootPath;
  private List<String> args;
  private ZKQuery query;

  /**
   * Generate a human readable description of the query.
   * 
   * @return Description based on the query parameters
   */
  public String generateDescription() {
    StringBuffer description = new StringBuffer();
    description.append(String.format(ZKPolicyDefs.Query.DESCRIPTION_FORMAT, rootPath, name) + "\n");
    description.append(" * "+ name + ": "+ query.getDescription());
    return description.toString();
  }
}