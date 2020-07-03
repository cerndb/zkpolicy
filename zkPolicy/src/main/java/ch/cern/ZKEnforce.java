package ch.cern;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

public class ZKEnforce {
  private ZKClient zk;
  private ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
  private ZKRollbackSet rollbackSet = new ZKRollbackSet();

  public ZKEnforce(ZKClient zk) {
    this.zk = zk;
  }

  public ZKEnforce(ZKClient zk, File rollbackStateFile) {
    this.zk = zk;
    this.rollbackSet.setOutputFile(rollbackStateFile);
  }

  /**
   * Execute the query in dry-run mode returning the znodes to be altered from
   * normal execution.
   * 
   * @param policy Policy to be enforced
   * @throws NoSuchMethodException     When the query provided is not implemented.
   * @throws SecurityException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws KeeperException
   * @throws InterruptedException
   * @throws NoSuchFieldException
   */
  public void enforceDry(ZKEnforcePolicyElement policy)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, KeeperException, InterruptedException, NoSuchFieldException {

    ZKQueryElement queryElement = policy.getQuery();
    if (this.zk.exists(queryElement.getRootPath(), null) == null) {
      System.out.println("The path " + queryElement.getRootPath() + " does not exist.");
      return;
    }
    ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
    ZKQuery query = zkDefaultQuery.getValueOf(queryElement.getName());
    enforceInnerDry(queryElement.getRootPath(), query, queryElement.getArgs());
  }

  /**
   * Enforce policy ACLs provided to every matching node of the requested query.
   * 
   * @param policy Policy to be enforced
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   * @throws InterruptedException
   * @throws KeeperException
   * @throws NoSuchFieldException
   */
  public void enforce(ZKEnforcePolicyElement policy)
      throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, InterruptedException, KeeperException, NoSuchFieldException {

    ZKQueryElement queryElement = policy.getQuery();
    if (this.zk.exists(queryElement.getRootPath(), null) == null) {
      System.out.println("The path " + queryElement.getRootPath() + " does not exist.");
      return;
    }
    List<ACL> policiesACL = new ArrayList<ACL>();
    for (String policyACLString : policy.getAcls()) {
      policiesACL.add(new ACLAugment(policyACLString).getACL());
    }
    ZKQuery query = this.zkDefaultQuery.getValueOf(queryElement.getName());
    enforceInner(policiesACL, queryElement.getRootPath(), query, queryElement.getArgs(), policy.isAppend());
    this.rollbackSet.exportToYAML();
  }

  private void enforceInnerDry(String path, ZKQuery query, List<String> queryACLs)
      throws InterruptedException, KeeperException {
    // Apply regex to path
    List<ACL> znodeACLList = null;
    List<String> children = null;

    // If the current user does not have READ permission for this znode, he cannot
    // getACL or ls so just skip it
    try {
      znodeACLList = this.zk.getACL(path, null);
      children = this.zk.getChildren(path, null);
    } catch (KeeperException e) {
      System.out.println("WARNING: No READ permission for " + path + ", skipping subtree");
      return;
    }

    if (query.query(znodeACLList, null, path, zk, queryACLs)) {
      System.out.println(path);
    }

    if (path.equals("/")) {
      path = "";
    }
    Collections.sort(children);
    for (String child : children) {
      this.enforceInnerDry(path + "/" + child, query, queryACLs);
    }
  }

  private void enforceInner(List<ACL> policies, String path, ZKQuery query, List<String> queryACLs, boolean append)
      throws KeeperException, InterruptedException {
    // Apply regex to path
    List<ACL> znodeACLList = null;
    List<String> children = null;
    List<ACL> newACLList = new ArrayList<ACL>();

    // If the current user does not have READ permission for this znode, he cannot
    // getACL or ls so just skip it
    try {
      znodeACLList = this.zk.getACL(path, null);
      children = this.zk.getChildren(path, null);
    } catch (KeeperException e) {
      System.out.println("WARNING: No READ permission for " + path + ", skipping subtree");
      return;
    }

    if (query.query(znodeACLList, null, path, zk, queryACLs)) {
      if (append) {
        newACLList.addAll(znodeACLList);
      }

      for (ACL policyACL : policies) {
        newACLList.add(policyACL);
      }
      // get here -P acls to add
      this.zk.setACL(path, newACLList, -1);
      rollbackSet.getElements().add(new ZKRollbackElement(path, znodeACLList));
    }

    if (path.equals("/")) {
      path = "";
    }
    Collections.sort(children);
    for (String child : children) {
      this.enforceInner(policies, path + "/" + child, query, queryACLs, append);
    }
  }
}