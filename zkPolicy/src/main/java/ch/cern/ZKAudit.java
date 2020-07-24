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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.common.X509Exception.SSLContextException;
import org.apache.zookeeper.data.ACL;

public class ZKAudit {
  private ZKClient zk;
  private ZKAuditSet zkAuditSet;
  private Hashtable<String, List<ZKQueryElement>> rootPathGroups;
  private Hashtable<String, List<ZKCheckElement>> rootPathCheckGroups;
  Hashtable<Integer, List<String>> queriesOutput = null;
  Hashtable<Integer, List<String>> checksOutput = null;

  /**
   * Get a set of unique root paths defined by the user for each query.
   * 
   * @return Query root path set
   */
  public Set<String> getRootPathKeys() {
    return this.rootPathGroups.keySet();
  }

  /**
   * Get a set of unique root paths defined by the user for each check.
   * 
   * @return Check root path set
   */
  public Set<String> getRootPathCheckKeys() {
    return this.rootPathCheckGroups.keySet();
  }

  /**
   * Return audit configuration object.
   * @return
   */
  public ZKAuditSet getZkAuditSet() {
    return this.zkAuditSet;
  }

  /**
   * Construct ZKAudit object for audit reports generation.
   * 
   * @param zk              ZooKeeper Client connected to a ZooKeeper Server
   * @param auditConfigFile File that defines the queries to be executed for this
   *                        particular audit report
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public ZKAudit(ZKClient zk, File auditConfigFile) throws JsonParseException, JsonMappingException, IOException {
    this.zk = zk;
    this.rootPathGroups = new Hashtable<String, List<ZKQueryElement>>();
    this.rootPathCheckGroups = new Hashtable<String, List<ZKCheckElement>>();
    this.zkAuditSet = new ZKAuditSet(auditConfigFile);
    if (this.zkAuditSet.getQueries() != null) {
      this.queriesOutput = ZKPolicyUtils.getQueryOutputBuffer(this.zkAuditSet.getQueries());
    }

    if (this.zkAuditSet.getChecks() != null) {
      this.checksOutput = ZKPolicyUtils.getChecksOutputBuffer(this.zkAuditSet.getChecks());
    }
  }

  /**
   * Generate a complete list of accessible znodes and their ACL.
   * 
   * @return LIst of znodes and their respective ACL
   * @throws KeeperException
   * @throws InterruptedException
   */
  public String getACLOverview() throws KeeperException, InterruptedException {
    List<String> output = new ArrayList<String>();
    output.add("Permission overview for ZooKeeper Tree\n");
    this.getACLHeaderInner("/", output);
    return String.join("\n", output) + "\n";
  }

  /**
   * Probe the ZooKeeper Server for whitelisted Four Letter Words.
   * 
   * @return List of enabled, disabled and unknown four letter words
   * @throws KeeperException
   * @throws InterruptedException
   * @throws IOException
   * @throws SSLContextException
   */
  public String getFourLetterWordOverview()
      throws KeeperException, InterruptedException, IOException, SSLContextException {
    List<String> enabledOutput = new ArrayList<String>();
    List<String> disabledOutput = new ArrayList<String>();
    List<String> unknownOutput = new ArrayList<String>();

    for (ZKPolicyDefs.FourLetterWords command : ZKPolicyDefs.FourLetterWords.values()) {
      String commandResponse = "";
      try {
        commandResponse = this.retrySend4LetterWord(command.getCommand(), 1000, 3);
      } catch (IOException e) {
        unknownOutput.add(command.getCommand());
        continue;
      }

      if (commandResponse.contains("is not executed because it is not in the whitelist.")) {
        disabledOutput.add(command.getCommand());
      } else {
        enabledOutput.add(command.getCommand());
      }
    }

    Collections.sort(enabledOutput);
    Collections.sort(disabledOutput);
    Collections.sort(unknownOutput);

    return "Four Letter Words overview for ZooKeeper Server\n" + "Enabled: " + String.join(", ", enabledOutput) + "\n"
        + "Disabled: " + String.join(", ", disabledOutput) + "\n" + "Unknown: " + String.join(", ", unknownOutput)
        + "\n";
  }

  /**
   * Send four letter words, retrying for a defined number of times.
   * 
   * @param command Four letter word to be sent
   * @param timeOut Timeout in order to resent the command
   * @param tryNum  Number of retries allowed
   * @return Result of four letter word command
   * @throws SSLContextException
   * @throws IOException
   */
  private String retrySend4LetterWord(String command, int timeOut, int tryNum) throws SSLContextException, IOException {
    int count = 0;
    while (true) {
      try {
        return FourLetterWordMain.send4LetterWord(this.zk.getHost(), this.zk.getPort(), command, false, timeOut);
      } catch (IOException e) {
        // handle exception
        if (++count == tryNum)
          throw e;
      }
    }
  }

  /**
   * Execute queries defined by the user in groups by their root path for optimal
   * execution.
   * 
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws KeeperException
   * @throws InterruptedException
   */
  private void executeRootGroupQueries() throws JsonParseException, JsonMappingException, IOException,
      NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
      InvocationTargetException, KeeperException, InterruptedException {

    ZKTree zkTree = new ZKTree(zk);

    this.groupQueriesByRootPath();

    for (String rootPath : this.getRootPathKeys()) {
      List<ZKQueryElement> currentBatch = this.rootPathGroups.get(rootPath);
      // Execute one for each batch of rootPaths
      zkTree.queryFind(rootPath, currentBatch, queriesOutput);
    }
  }

  private void executeRootGroupChecks() throws JsonParseException, JsonMappingException, IOException,
      NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
      InvocationTargetException, KeeperException, InterruptedException {

    ZKCheck zkCheck = new ZKCheck(this.zk);
    this.groupChecksByRootPath();

    for (String rootPath : this.getRootPathCheckKeys()) {
      List<ZKCheckElement> currentBatch = this.rootPathCheckGroups.get(rootPath);
      // Execute one for each batch of rootPaths
      zkCheck.check(rootPath, currentBatch, checksOutput);
    }
  }

  /**
   * Generate queries result section.
   * 
   * @return Audir report section with queries results
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws KeeperException
   * @throws InterruptedException
   */
  public String generateQueriesSection() throws JsonParseException, JsonMappingException, IOException,
      NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
      InvocationTargetException, KeeperException, InterruptedException {

    StringBuffer outputBuf = new StringBuffer();

    // Parse output buffers for queries
    if (zkAuditSet.getQueries() != null) {
      this.executeRootGroupQueries();
      for (ZKQueryElement queryElement : zkAuditSet.getQueries()) {
        outputBuf.append("\nQuery: " + queryElement.getName() + "\n");
        outputBuf.append("Root Path: " + queryElement.getRootPath() + "\n");

        if (queryElement.getArgs() != null) {
          outputBuf.append("Arguments:" + "\n- " + String.join("\n- ", queryElement.getArgs()) + "\n");
        }

        outputBuf.append("Description: " + queryElement.generateDescription() + "\n");

        outputBuf.append("\nResult:\n");
        outputBuf.append(String.join("\n", queriesOutput.get(queryElement.hashCode())) + "\n");
        outputBuf.append("\n" + ZKPolicyDefs.TerminalConstants.subSectionSeparator + "\n");
      }
    }
    return outputBuf.toString();
  }

  /**
   * Generate checks result section.
   * 
   * @return Audit report section with check results
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws KeeperException
   * @throws InterruptedException
   */
  public String generateChecksSection() throws JsonParseException, JsonMappingException, IOException,
      NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
      InvocationTargetException, KeeperException, InterruptedException {

    int aggregateCheckSuccessNum = 0;
    StringBuffer outputBuf = new StringBuffer();

    // Parse output buffers for checks
    if (zkAuditSet.getChecks() != null) {
      this.executeRootGroupChecks();
      for (ZKCheckElement checkElement : zkAuditSet.getChecks()) {
        outputBuf.append("\nCheck: " + checkElement.getTitle() + "\n");
        outputBuf.append("Root Path: " + checkElement.getRootPath() + "\n");
        outputBuf.append("Path Pattern: " + checkElement.getPathPattern() + "\n");

        if (checkElement.getAcls() != null) {
          outputBuf.append("Arguments:" + "\n- " + String.join("\n- ", checkElement.getAcls()) + "\n");
        }

        outputBuf.append("Description: " + checkElement.generateDescription() + "\n");

        if (checkElement.$status) {
          outputBuf.append("\nResult: PASS\n");
          aggregateCheckSuccessNum++;
        } else {
          outputBuf.append("\nResult: FAIL\n");
        }

        List<String> checkOutput = checksOutput.get(checkElement.hashCode());
        if (checkOutput.size() == 0) {
          outputBuf.append("No znodes matching the requested path pattern found.");
        }
        else {
          outputBuf.append(String.join("\n", checkOutput) + "\n");
        }

        outputBuf.append("\n" + ZKPolicyDefs.TerminalConstants.subSectionSeparator + "\n");
      }

      // Add aggregate result for checks
      if (aggregateCheckSuccessNum == zkAuditSet.getChecks().size()) {
        outputBuf.append("\nOverall Check Result: PASS\n");
      } else {
        outputBuf.append("\nOverall Check Result: FAIL\n");
        outputBuf.append("\nPASS: " + aggregateCheckSuccessNum + ", FAIL: ");
        outputBuf.append((zkAuditSet.getChecks().size() - aggregateCheckSuccessNum) + "\n");
      }
    }

    return outputBuf.toString();
  }

  /**
   * Recursively parse the znode tree for ACL overview generation.
   * 
   * @param path   Starting path for recursive traversal
   * @param output Output buffer
   * @throws KeeperException
   * @throws InterruptedException
   */
  private void getACLHeaderInner(String path, List<String> output) throws KeeperException, InterruptedException {
    List<ACL> acl;
    List<String> children = null;
    try {
      acl = this.zk.getACL(path, null);
      children = this.zk.getChildren(path, null);
    } catch (NoAuthException e) {
      output.add("Warning: No READ permission for " + path + ", skipping this subtree");
      return;
    }
    // Add information for this znode in for path - ACL
    StringBuffer aclOutput = new StringBuffer();
    List<ACLAugment> aclAugmentList = ACLAugment.generateACLAugmentList(acl);

    Iterator<ACLAugment> aclAugmentIterator = aclAugmentList.iterator();
    while (aclAugmentIterator.hasNext()) {
      ACLAugment aclAugment = aclAugmentIterator.next();
      aclOutput.append(aclAugment.getStringFromACL());
      if (aclAugmentIterator.hasNext()) {
        aclOutput.append(", ");
      }
    }

    output.add(path + " - " + aclOutput.toString());

    Collections.sort(children);

    if (path.equals("/")) {
      path = "";
    }

    Iterator<String> iterator = children.iterator();
    while (iterator.hasNext()) {
      String child = iterator.next();
      this.getACLHeaderInner(path + "/" + child, output);
    }
  }

  /**
   * Group queries based on their root path for optimal performance.
   * 
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void groupQueriesByRootPath()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();

    for (ZKQueryElement queryElement : this.zkAuditSet.getQueries()) {
      ZKQuery query = zkDefaultQuery.getValueOf(queryElement.getName());
      queryElement.setQuery(query);

      if (this.rootPathGroups.containsKey(queryElement.getRootPath())) {
        this.rootPathGroups.get(queryElement.getRootPath()).add(queryElement);
      } else {
        List<ZKQueryElement> batchList = new ArrayList<ZKQueryElement>();
        batchList.add(queryElement);
        this.rootPathGroups.put(queryElement.getRootPath(), batchList);
      }
    }
  }

  /**
   * Group checks based on their root path for optimal performance.
   * 
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  public void groupChecksByRootPath()
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

    for (ZKCheckElement checkElement : this.zkAuditSet.getChecks()) {

      if (this.rootPathCheckGroups.containsKey(checkElement.getRootPath())) {
        this.rootPathCheckGroups.get(checkElement.getRootPath()).add(checkElement);
      } else {
        List<ZKCheckElement> batchList = new ArrayList<ZKCheckElement>();
        batchList.add(checkElement);
        this.rootPathCheckGroups.put(checkElement.getRootPath(), batchList);
      }
    }
  }

}