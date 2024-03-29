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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Export ZooKeeper tree functionality.
 */
public class ZKExport {
  private static Logger logger = LogManager.getLogger(ZKExport.class);

  private ZKClient zk;
  private ZKTreeNode znodeRoot;

  public ZKExport(ZKClient zk) {
    this.zk = zk;
    this.znodeRoot = new ZKTreeNode();
  }

  /**
   * Export a znode subtree to certain output format.
   * 
   * @param rootPath    Path to start recursively exporting znodes
   * @param format      Export format
   * @param compactMode Enable minified mode for export file
   * @param outputFile  Output file path
   */
  public void export(String rootPath, ZKPolicyDefs.ExportFormats format, boolean compactMode, File outputFile) {
    try {
      this.toTreeStruct(rootPath, znodeRoot);
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }

    switch (format) {
      case json:
        this.exportToJSON(outputFile, compactMode);
        break;
      case yaml:
        this.exportToYAML(outputFile);
        break;
      default:
        break;
    }
  }

  /**
   * Export znode subtree to JSON.
   */
  private void exportToJSON(File outputFile, boolean compactMode) {
    try {
      ObjectMapper mapper = new ObjectMapper(new JsonFactory());
      if (compactMode) {
        mapper.writeValue(outputFile, znodeRoot);
      } else {
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, znodeRoot);
      }
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  /**
   * Export znode subtree to YAML.
   */
  private void exportToYAML(File outputFile) {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.writeValue(outputFile, znodeRoot);
    } catch (Exception e) {
      System.out.println(e.toString());
      logger.error("Exception occurred!", e);
    }
  }

  /**
   * Recursive function that constructs the full ZNode tree.
   */
  private void toTreeStruct(String path, ZKTreeNode currentNode) throws KeeperException, InterruptedException {
    byte[] data;
    List<ACL> acl;
    Stat stat = new Stat();
    try {
      data = this.zk.getData(path, null, stat); // fill the stat afterwards
      acl = this.zk.getACL(path, null);
    } catch (NoAuthException e) {
      return;
    }
    List<Byte> byteData = new ArrayList<Byte>();
    if (data != null) {
      for (byte dataItem : data) {
        byteData.add(dataItem);
      }
    }

    currentNode.setData(byteData);
    currentNode.setPath(path);
    currentNode.setAcl(acl);
    currentNode.setStat(stat);

    List<String> children = null;
    try {
      children = this.zk.getChildren(path, null);
    } catch (NoAuthException e) {
      return;
    }

    Collections.sort(children);

    if (path.equals("/")) {
      path = "";
    }

    Iterator<String> iterator = children.iterator();
    List<ZKTreeNode> childrenList = new ArrayList<ZKTreeNode>();
    while (iterator.hasNext()) {
      String child = iterator.next();
      ZKTreeNode childNode = new ZKTreeNode();
      this.toTreeStruct(path + "/" + child, childNode);
      childrenList.add(childNode);
    }
    currentNode.setChildren(childrenList);
  }

}