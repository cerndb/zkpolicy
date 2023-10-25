/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.util.Iterator;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Check element as defined in the audit report configuration file.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ZKCheckElement {
  private String title;
  private String rootPath;
  private String pathPattern;
  private Boolean negate;
  List<String> acls;

  // Ignore chekstyle violation because lombok uses `$` prefix
  // to indicate member fields to be ignored from code generation.
  @SuppressWarnings("checkstyle:membername")
  public boolean $status = true;

  /**
   * Generate a human readable description of the check.
   * 
   * @return Description based on the check parameters
   */
  public String generateDescription() {
    StringBuffer outputBuf = new StringBuffer();
    if (this.negate != null && this.negate) {
      outputBuf.append(String.format(ZKPolicyDefs.Check.NEGATE_DESCRIPTION_FORMAT, rootPath, pathPattern) + "\n");
    } else {
      outputBuf.append(String.format(ZKPolicyDefs.Check.DESCRIPTION_FORMAT, rootPath, pathPattern) + "\n");
    }

    Iterator<String> aclIterator = acls.iterator();
    while (aclIterator.hasNext()) {
      String acl = aclIterator.next();
      outputBuf.append("- " + acl);
      if (aclIterator.hasNext()) {
        outputBuf.append("\n");
      }
    }
    return outputBuf.toString();
  }
}