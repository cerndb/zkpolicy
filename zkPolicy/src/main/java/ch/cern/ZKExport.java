package ch.cern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

public class ZKExport {
    ZKClient zk ;
    

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
    public void export(String rootPath, ZKPolicyDefs.Formats format, boolean compactMode, File outputFile) {
        switch (format) {
            case json:
                this.exportToJSON(outputFile, rootPath, compactMode);
                break;
            default:
                break;
        }
    }

    /**
     * Export znode subtree to JSON
     */
    private void exportToJSON(File outputFile, String rootPath, boolean compactMode) {
        ZKTreeNode root = new ZKTreeNode();
        Gson gson;
        try {
            this.toTreeStruct(rootPath, root);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

        if (compactMode) {
            gson = new GsonBuilder().create();
        } else {
            gson = new GsonBuilder().setPrettyPrinting().create();
        }

        try {
            Writer writer = new FileWriter(outputFile);
            gson.toJson(root, writer);
            writer.flush(); // flush data to file <---
            writer.close(); // close write <---
        } catch (JsonIOException | IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * recursive function that constructs the full ZNode tree
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