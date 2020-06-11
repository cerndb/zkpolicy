package ch.cern;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.data.ACL;

/**
 * Class that offers check functionality of a znode (or znodes matching a path
 * pattern) against a specific ACL definition.
 */
public class ZKCheck {
  private ZKClient zk;
  private ZKDefaultQuery defaultQueries = new ZKDefaultQuery();
  private static Logger logger = LogManager.getLogger(ZKCheck.class);

  public ZKCheck(ZKClient zk) {
    this.zk = zk;
  }

  /**
   * Execute check operations starting recursively from the rootPath.
   * 
   * @param rootPath      Path to start the recursive check
   * @param checkElements Check operations to be executed on each znode
   * @param checksOutput  Output for the check operations
   * @throws KeeperException
   * @throws InterruptedException
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws NoSuchFieldException
   */
  public void check(String rootPath, List<ZKCheckElement> checkElements, Hashtable<Integer, List<String>> checksOutput)
      throws KeeperException, InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException, NoSuchFieldException {

    List<ZKCheckElement> invalidChecks = new ArrayList<ZKCheckElement>();
    for (ZKCheckElement zkCheckElement : checkElements) {
      // validate root path requested:
      try {
        if (this.zk.exists(zkCheckElement.getRootPath(), null) == null) {
          checksOutput.get(zkCheckElement.hashCode())
              .add("The path " + zkCheckElement.getRootPath() + " does not exist.");
          invalidChecks.add(zkCheckElement);
          continue;
        }
      } catch (IllegalArgumentException e) {
        checksOutput.get(zkCheckElement.hashCode())
            .add("Invalid rootpath " + zkCheckElement.getRootPath() + " : " + e.getMessage());
        invalidChecks.add(zkCheckElement);
        continue;
      }

    }

    for (ZKCheckElement zkQueryElement : invalidChecks) {
      checkElements.remove(zkQueryElement);
    }

    if (checkElements.size() > 0) {
      this.checkIntPreOrder(rootPath, checkElements, checksOutput);
    }
  }

  private void checkIntPreOrder(String path, List<ZKCheckElement> checkElements,
      Hashtable<Integer, List<String>> checksOutput) throws KeeperException, InterruptedException, NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {

    List<String> children = null;
    List<ACL> znodeACLList = null;

    try {
      children = this.zk.getChildren(path, null);
      znodeACLList = this.zk.getACL(path, null);
    } catch (NoAuthException e) {
      logger.info("WARNING: No READ permission for " + path + ", skipping subtree");
    }

    for (ZKCheckElement zkCheckElement : checkElements) {
      // First check if check element's path pattern satisfies the current one
      Pattern pathPatternRegex = Pattern.compile(zkCheckElement.getPathPattern());
      Matcher currentMatcher = pathPatternRegex.matcher(path);
      if (currentMatcher.matches()) {
        // If the path name matches check whether negate is enabled and execute query

        ZKQuery checkQuery;
        if (zkCheckElement.getNegate()) {
          checkQuery = defaultQueries.noSatisfyACL;
        } else {
          checkQuery = defaultQueries.exactACL;
        }

        if (checkQuery.query(znodeACLList, null, path, zk, zkCheckElement.getAcls())) {
          checksOutput.get(zkCheckElement.hashCode()).add(path + " : " + "PASS");
        } else {
          // Check if there was permission for this subtree
          if (znodeACLList == null) {
            checksOutput.get(zkCheckElement.hashCode()).add(path + " : " + "FAIL (No permission for subtree)");
          } else {
            checksOutput.get(zkCheckElement.hashCode()).add(path + " : " + "FAIL (actual: " + ACLAugment.generateACLStringList(znodeACLList)+ ")");
          }
          zkCheckElement.$status = false;
        }
      }
    }

    if (path.equals("/")) {
      path = "";
    }

    if (children == null) {
      return;
    } else {
      Collections.sort(children);
      for (String child : children) {
        this.checkIntPreOrder(path + "/" + child, checkElements, checksOutput);
      }
    }
  }
}