package ch.cern;

import java.util.List;
import org.apache.zookeeper.data.ACL;
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
public class ZKRollbackElement {

  private String path;
  private List<ACL> acl;
}