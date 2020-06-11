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
    if (this.negate) {
      outputBuf.append("Check if the subset of znodes under " + rootPath + " with path that matches the " + pathPattern
          + " pattern are not accessible by clients with the following authentication credentials:\n");
    } else {
      outputBuf.append("Check if the subset of znodes under " + rootPath + " with path that matches the " + pathPattern
          + " pattern have the following ACL definition set:\n");
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