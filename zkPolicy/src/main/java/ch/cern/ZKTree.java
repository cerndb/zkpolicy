package ch.cern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * Class that handles operations on the ZNode tree structure
 */
public class ZKTree {
    private ZooKeeper zk;

    // colors for print
    private static final String ANSI_RESET = "\u001B[0m";

    private ZKConfig config;

    /**
     *
     * @param zk A ZooKeeper instance that provides access to the ZNode tree
     */
    public ZKTree(ZooKeeper zk, ZKConfig config) {
        this.zk = zk;
        this.config = config;
    }

    /**
     * Function to return a tree view of the selected sub-tree with query matching
     * nodes colored green and non matching red
     *
     * @param path     Path from where to start the recursive query search
     * @return String ready to be printed in tree format
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public String queryTree(String path, String[] optionArgs)
            throws KeeperException, InterruptedException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        List<String> output = new ArrayList<String>();
        ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
        Method method = zkDefaultQuery.getClass().getMethod(optionArgs[0]);

        if (optionArgs[0].equals("parentYesChildNo")) {
            this.queryTreeIntParentYesChildNo(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), optionArgs,
                    null);
        } else {
            this.queryTreeIntPreOrder(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), optionArgs);
        }

        return this.colorCodeExplanation() + String.join("", output) + '\n';
    }

    // recursive function that constructs the full ZNode tree
    private void queryTreeIntPreOrder(List<String> output, String path, String indent, String name, ZKQuery query,
            String[] optionArgs) throws KeeperException, InterruptedException {
        List<String> children = null;
        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        List<ACL> znodeACLList = this.zk.getACL(path, null);

        String znodePrintColor = "";
        if (query.query(znodeACLList, path, this.zk, optionArgs)) {
            znodePrintColor = config.getMatchcolorvalue();
        } else {
            znodePrintColor = config.getMismatchcolorvalue();
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
            this.queryTreeIntPreOrder(output, path + "/" + child, indent, child, query, optionArgs);
        }
    }

    private void queryTreeIntParentYesChildNo(List<String> output, String path, String indent, String name,
            ZKQuery query, String[] optionArgs, List<ACL> parentACLList) throws KeeperException, InterruptedException {
        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        String znodePrintColor = "";

        if (parentACLList == null || query.query(parentACLList, path, this.zk, optionArgs)) {
            znodePrintColor = config.getMatchcolorvalue();
        } else {
            znodePrintColor = config.getMismatchcolorvalue();
        }
        parentACLList = this.zk.getACL(path, null);

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
        output.add(znodePrintColor + indent + ZKTree.ANSI_RESET + "/" + name + "\n");

        Collections.sort(children);
        for (String child : children) {
            this.queryTreeIntParentYesChildNo(output, path + "/" + child, indent, child, query, optionArgs,
                    parentACLList);
        }
    }

    /**
     * Function to return a list of the selected sub-tree with the full path of
     * query matching nodes
     *
     * @param path    path Path from where to start the recursive query search
     * @param optionArgs Arguments array passed through CLI
     * @return String ready to be printed in list format.
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public String queryFind(String path, String[] optionArgs)
            throws KeeperException, InterruptedException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<String> output = new ArrayList<String>();

        ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
        Method method = zkDefaultQuery.getClass().getMethod(optionArgs[0]);

        if (optionArgs[0].equals("parentYesChildNo")) {
            this.queryFindIntParentYesChildNo(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), optionArgs,
                    null);
        } else {
            this.queryFindIntPreOrder(output, path, "", "", (ZKQuery) method.invoke(zkDefaultQuery), optionArgs);
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

    private String colorCodeExplanation() {
        String explanation = "";
        explanation += "* " + config.getMatchcolorvalue() + config.getMatchcolorname() + ":\t" + ZKTree.ANSI_RESET
                + " znodes matching the query" + "\n";
        explanation += "* " + config.getMismatchcolorvalue() + config.getMismatchcolorname() + ":\t\t"
                + ZKTree.ANSI_RESET + " znodes not matching the query" + "\n";
        return explanation;
    }
}