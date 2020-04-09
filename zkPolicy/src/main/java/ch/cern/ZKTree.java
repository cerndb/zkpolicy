package ch.cern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * Class that handles operations on the ZNode tree structure.
 */
public class ZKTree {
    private ZooKeeper zk;

    private ZKConfig config;

    private String resetColor;
    private String matchColor;
    private String misMatchColor;

    /**
     *
     * @param zk A ZooKeeper instance that provides access to the ZNode tree
     */
    public ZKTree(ZooKeeper zk, ZKConfig config) {
        this.zk = zk;
        this.config = config;
        this.resetColor = ZKPolicyDefs.Colors.valueOf("RESET").getANSIValue();
        this.matchColor = ZKPolicyDefs.Colors.valueOf(config.getMatchcolor()).getANSIValue();
        this.misMatchColor = ZKPolicyDefs.Colors.valueOf(config.getMismatchcolor()).getANSIValue();
    }

    /**
     * Function to return a tree view of the selected sub-tree with query matching
     * nodes colored green and non matching red
     *
     * @param queryACLs Query ACL parameters
     * @return String ready to be printed in tree format
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public String queryTree(String queryName, String rootPath, String[] queryACLs) throws KeeperException, InterruptedException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        List<String> output = new ArrayList<String>();
        ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();

        Method method = zkDefaultQuery.getClass().getMethod(queryName);

        String path = rootPath;

        if (queryName.equals("parentYesChildNo")) {
            this.queryTreeIntParentYesChildNo(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), queryACLs,
                    null, true, false, false);
        } else {
            this.queryTreeIntPreOrder(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), queryACLs, true,
                    false, false);
        }

        return this.colorCodeExplanation() + String.join("", output) + '\n';
    }

    // recursive function that constructs the full ZNode tree
    private void queryTreeIntPreOrder(List<String> output, String path, String indent, String name, ZKQuery query,
            String[] optionArgs, boolean isQueryRoot, boolean isLast, boolean isParentLast)
            throws KeeperException, InterruptedException {
        List<String> children = null;
        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        List<ACL> znodeACLList = this.zk.getACL(path, null);

        String znodePrintColor = "";
        if (query.query(znodeACLList, path, this.zk, optionArgs)) {
            znodePrintColor = this.matchColor;
        } else {
            znodePrintColor = this.misMatchColor;
        }

        if (path.equals("/")) {
            path = "";
        } else if (isQueryRoot) {
            name = path.substring(1, path.length());
        } else {
            if (name.equals("")) {
                name = path.substring(1, path.length());
            }
 
            if (indent.length() > 0) {
                if (isParentLast) {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.lastParentIndent;
                } else {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.innerParentIndent;
                }
            }
            if (isLast) {
                indent += ZKPolicyDefs.TerminalConstants.lastChildIndent;
            } else {
                indent += ZKPolicyDefs.TerminalConstants.innerChildIndent;
            }

        }
        output.add(indent + znodePrintColor + "/" + name + this.resetColor + "\n");

        Collections.sort(children);

        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            this.queryTreeIntPreOrder(output, path + "/" + child, indent, child, query, optionArgs, false,
                    !iterator.hasNext(), isLast);
        }
    }

    private void queryTreeIntParentYesChildNo(List<String> output, String path, String indent, String name,
            ZKQuery query, String[] optionArgs, List<ACL> parentACLList, boolean isQueryRoot, boolean isLast,
            boolean isParentLast) throws KeeperException, InterruptedException {
        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        String znodePrintColor = "";

        if (parentACLList == null || query.query(parentACLList, path, this.zk, optionArgs)) {
            znodePrintColor = this.matchColor;
        } else {
            znodePrintColor = this.misMatchColor;
        }
        parentACLList = this.zk.getACL(path, null);

        if (path.equals("/")) {
            path = "";
        } else if (isQueryRoot) {
            name = path.substring(1, path.length());
        } else {
            if (name.equals("")) {
                name = path.substring(1, path.length());
            }

            if (indent.length() > 0) {
                if (isParentLast) {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.lastParentIndent;
                } else {
                    indent = indent.substring(0, indent.length() - ZKPolicyDefs.TerminalConstants.indentStepLength)
                            + ZKPolicyDefs.TerminalConstants.innerParentIndent;
                }
            }
            if (isLast) {
                indent += znodePrintColor + ZKPolicyDefs.TerminalConstants.lastChildIndent;
            } else {
                indent += znodePrintColor + ZKPolicyDefs.TerminalConstants.innerChildIndent;
            }
        }
        output.add(indent + this.resetColor + "/" + name + "\n");

        Collections.sort(children);

        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            this.queryTreeIntParentYesChildNo(output, path + "/" + child, indent, child, query, optionArgs,
                    parentACLList, false, !iterator.hasNext(), isLast);
        }
    }

    /**
     * Function to return a list of the selected sub-tree with the full path of
     * query matching nodes
     *
     * @param queryACLs Query ACL parameters
     * @return String ready to be printed in list format.
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public String queryFind(String queryName, String rootPath, String[] queryACLs) throws KeeperException, InterruptedException, NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        List<String> output = new ArrayList<String>();

        ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
        Method method = zkDefaultQuery.getClass().getMethod(queryName);

        String path = rootPath;
        if (queryName.equals("parentYesChildNo")) {
            this.queryFindIntParentYesChildNo(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), queryACLs,
                    null);
        } else {
            this.queryFindIntPreOrder(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), queryACLs);
        }

        return '\n' + String.join("", output) + '\n';
    }

    // recursive function that constructs the full ZNode tree
    private void queryFindIntPreOrder(List<String> output, String path, String indent, String name, ZKQuery query,
            String[] optionArgs) throws KeeperException, InterruptedException {

        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }
        List<ACL> znodeACLList = this.zk.getACL(path, null);

        if (query.query(znodeACLList, path, this.zk, optionArgs)) {

            output.add(path + "\n");
        }
        if (path.equals("/")) {
            path = "";
        }

        Collections.sort(children);
        for (String child : children) {
            this.queryFindIntPreOrder(output, path + "/" + child, indent, child, query, optionArgs);
        }
    }

    // recursive function that constructs the full ZNode tree
    private void queryFindIntParentYesChildNo(List<String> output, String path, String indent, String name,
            ZKQuery query, String[] optionArgs, List<ACL> parentACLList) throws KeeperException, InterruptedException {

        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        if (parentACLList != null && !query.query(parentACLList, path, this.zk, optionArgs)) {
            output.add(path + "\n");
        }
        parentACLList = this.zk.getACL(path, null);

        if (path.equals("/")) {
            path = "";
        }

        Collections.sort(children);
        for (String child : children) {
            this.queryFindIntParentYesChildNo(output, path + "/" + child, indent, child, query, optionArgs,
                    parentACLList);
        }
    }

    public void export(String rootPath, ZKExportCli.Format format, boolean compactMode, File outputFile) {

        switch (format) {
            case json:
                this.exportToJSON(outputFile, rootPath, compactMode);
                break;
            default:
                break;
        }
    }

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

    // recursive function that constructs the full ZNode tree
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

    private String colorCodeExplanation() {
        String explanation = "";
        explanation += "* " + this.matchColor + config.getMatchcolor() + ":\t" + this.resetColor
                + " znodes matching the query" + "\n";
        explanation += "* " + this.misMatchColor + config.getMismatchcolor() + ":\t\t"
                + this.resetColor + " znodes not matching the query" + "\n";
        return explanation;
    }
}