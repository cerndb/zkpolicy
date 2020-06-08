package ch.cern;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class that holds configuration for policies as defined in the enforcing yaml
 * configuration file.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKEnforcePolicyElement {

  private String title;
  private ZKQueryElement query;
  private boolean append;
  private List<String> acls;
}