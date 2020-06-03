package ch.cern;

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
  private String[] acls;
  //CHECKSTYLE.OFF: MemberName
  public boolean $status = true;
  //CHECKSTYLE.ON : MemberName
}