package ch.cern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * Class that handles operations on the ZNode tree structure
 */
public class ZKTree {
    private ZooKeeper zk;

    // colors for print
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";

    /**
     * 
     * @param zk A ZooKeeper instance that provides access to the ZNode tree
     */
    public ZKTree(ZooKeeper zk) {
        this.zk = zk;
    }

    /**
     * Function to return a tree view of the selected sub-tree with query matching
     * nodes colored green and non matching red
     * 
     * @param path  Path from where to start the recursive query search
     * @param query The actual query to be executed, in lambda function form
     * @return String ready to be printed in tree format.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String queryTree(String path, ZKQuery query) throws KeeperException, InterruptedException {
        List<String> output = new ArrayList<String>();
        this.queryTreeInt(output, path, "", "", query);
        return '\n' + String.join("", output) + '\n';
    }

    // recursive function that constructs the full ZNode tree
    private void queryTreeInt(List<String> output, String path, String indent, String name, ZKQuery query)
            throws KeeperException, InterruptedException {

        List<String> children = this.zk.getChildren(path, null);
        List<ACL> znodeACLList = this.zk.getACL(path, null);

        String znodePrintColor = "";
        if (query.query(znodeACLList)) {
            znodePrintColor = ZKTree.ANSI_GREEN;
        } else {
            znodePrintColor = ZKTree.ANSI_RED;
        }

        if (path.equals("/")) {
            path = "";
        } else {
            if (name.equals("")) {
                name = path.substring(1, path.length());
            }
            // indent.substring(0, indent.length() - 5);
            if (indent.length() > 0) {
                indent = indent.substring(0, indent.length() - 5) + "       ";
            }
            indent += "├─── ";
        }
        output.add(indent + znodePrintColor + "/" + name + ZKTree.ANSI_RESET + "\n");

        Collections.sort(children);
        for (String child : children) {
            this.queryTreeInt(output, path + "/" + child, indent, child, query);
        }
    }

    /**
     * Function to return a list of the selected sub-tree with the full path of
     * query matching nodes
     * 
     * @param path  path Path from where to start the recursive query search
     * @param query query The actual query to be executed, in lambda function form
     * @return String ready to be printed in list format.
     * @throws KeeperException
     * @throws InterruptedException
     */
    public String queryFind(String path, ZKQuery query) throws KeeperException, InterruptedException {
        List<String> output = new ArrayList<String>();
        this.queryFindInt(output, path, "", "", query);
        return '\n' + String.join("", output) + '\n';
    }

    // recursive function that constructs the full ZNode tree
    private void queryFindInt(List<String> output, String path, String indent, String name, ZKQuery query)
            throws KeeperException, InterruptedException {

        List<String> children = this.zk.getChildren(path, null);
        List<ACL> znodeACLList = this.zk.getACL(path, null);

        if (query.query(znodeACLList)) {

            output.add(path + "\n");
        }
        if (path.equals("/")) {
            path = "";
        }

        Collections.sort(children);
        for (String child : children) {
            this.queryFindInt(output, path + "/" + child, indent, child, query);
        }
    }

}