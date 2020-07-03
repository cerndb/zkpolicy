package ch.cern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.zookeeper.KeeperException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

/**
 * Class that stores information for rollback to previous state after enforcing.
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKRollbackSet {

  private List<ZKRollbackElement> elements = new ArrayList<ZKRollbackElement>();
  // Ignore chekstyle violation because lombok uses `$` prefix
  // to indicate member fields to be ignored from code generation.
  @SuppressWarnings("checkstyle:membername")
  private File $outputFile;

  public ZKRollbackSet(File policyConfigFile) throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper om = new ObjectMapper(new YAMLFactory());
    om.readerForUpdating(this).readValue(policyConfigFile);
  }

  public void setOutputFile(File rollbackStateFile) {
    this.$outputFile = rollbackStateFile;
  }

  /**
   * Export RollbackSet to YAML file.
   */
  public void exportToYAML() {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.writeValue(this.$outputFile, this);
    } catch (Exception e) {
      System.out.println(e.toString());
    }
  }

  /**
   * Rollback to pre enforce state.
   * 
   * @param zk ZooKeeper client for setACL
   * @throws KeeperException
   * @throws InterruptedException
   */
  public void enforceRollback(ZKClient zk) throws KeeperException, InterruptedException {
    for (ZKRollbackElement zkRollbackElement : elements) {
      try {
        zk.setACL(zkRollbackElement.getPath(), zkRollbackElement.getAcl(), -1);
      } catch (KeeperException.NoNodeException e) {
        System.out.println(e.toString());
      }
    }
  }
}