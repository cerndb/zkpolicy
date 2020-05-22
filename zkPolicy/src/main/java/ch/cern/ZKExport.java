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
 * Export ZooKeeper tree functionality
 */
public class ZKExport {
    private static Logger logger = LogManager.getLogger(ZKExport.class);

    private ZKClient zk ;

    public ZKExport(ZKClient zk){
        this.zk = zk;
    }

    /**
     * Export a znode subtree to certain output format
     * 
     * @param rootPath    Path to start recursively exporting znodes
     * @param format      Export format
     * @param compactMode Enable minified mode for export file
     * @param outputFile  Output file path
     */
    public void export(String rootPath, ZKPolicyDefs.ExportFormats format, boolean compactMode, File outputFile) {
        switch (format) {
            case json:
                this.exportToJSON(outputFile, rootPath, compactMode);
                break;
            case yaml:
                this.exportToYAML(outputFile, rootPath, compactMode);
            default:
                break;
        }
    }

    /**
     * Export znode subtree to JSON
     */
    private void exportToJSON(File outputFile, String rootPath, boolean compactMode) {
        ZKTreeNode root = new ZKTreeNode();
        try {
            this.toTreeStruct(rootPath, root);
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }

        try {
            ObjectMapper mapper = new ObjectMapper(new JsonFactory());
            if (compactMode) {
                mapper.writeValue(outputFile, root);
            } else {
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, root);
            }
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }
    }

    /**
     * Export znode subtree to YAML
     */
    private void exportToYAML(File outputFile, String rootPath, boolean compactMode) {
        ZKTreeNode root = new ZKTreeNode();
        try {
            this.toTreeStruct(rootPath, root);
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }

        try {
            // We ignore compactMode for YAML files
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.writeValue(outputFile, root);
        } catch (Exception e) {
            System.out.println(e.toString()); 
            logger.error("Exception occurred!", e);
        }
    }

    /**
     * Recursive function that constructs the full ZNode tree
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
        currentNode.setData(data);
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
        currentNode.setChildren(childrenList.toArray(new ZKTreeNode[childrenList.size()]));
    }

}