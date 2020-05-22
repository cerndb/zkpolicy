package ch.cern;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoAuthException;
import org.apache.zookeeper.data.ACL;

/**
 * Class that handles operations on the ZNode tree structure.
 */
public class ZKTree {
    private ZKClient zk;

    private String resetColor;
    private String matchColor;
    private String misMatchColor;

    /**
     *
     * @param zk A ZooKeeper instance that provides access to the ZNode tree
     */
    public ZKTree(ZKClient zk) {
        this.zk = zk;
        this.resetColor = ZKPolicyDefs.Colors.valueOf("RESET").getANSIValue();
        this.matchColor = ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMatchColor()).getANSIValue();
        this.misMatchColor = ZKPolicyDefs.Colors.valueOf(zk.getZKPConfig().getMismatchColor()).getANSIValue();
    }

    /**
     * Function to return a tree view of the selected sub-tree with query matching
     * nodes colored green and non matching red
     *
     * @param rootPath      Path to start recursive query execution from
     * @param queryElements List of queries to be executed on each node
     * @param queriesOutput Output buffers for each query
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public void queryTree(String rootPath, List<ZKQueryElement> queryElements,
            Hashtable<Integer, List<String>> queriesOutput)
            throws KeeperException, InterruptedException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {

        List<ZKQueryElement> invalidQueries = new ArrayList<ZKQueryElement>();
        List<ZKQueryElement> parentYesChildNoQueries = new ArrayList<ZKQueryElement>();

        for (ZKQueryElement zkQueryElement : queryElements) {
            // validate root path requested:
            try {
                if (this.zk.exists(zkQueryElement.getRootPath(), null) == null) {
                    queriesOutput.get(zkQueryElement.hashCode())
                            .add("The path " + zkQueryElement.getRootPath() + " does not exist.");
                    invalidQueries.add(zkQueryElement);
                    continue;
                }
            } catch (IllegalArgumentException e) {
                queriesOutput.get(zkQueryElement.hashCode())
                        .add("Invalid rootpath " + zkQueryElement.getRootPath() + " : " + e.getMessage());
                invalidQueries.add(zkQueryElement);
                continue;
            }
            if (zkQueryElement.getName().equals("parentYesChildNo")) {
                this.queryTreeIntParentYesChildNo(zkQueryElement.getRootPath(), "", "", null, true, false, false,
                        queriesOutput, zkQueryElement);
                parentYesChildNoQueries.add(zkQueryElement);
            }

        }

        for (ZKQueryElement zkQueryElement : parentYesChildNoQueries) {
            queryElements.remove(zkQueryElement);
        }

        for (ZKQueryElement zkQueryElement : invalidQueries) {
            queryElements.remove(zkQueryElement);
        }

        if (queryElements.size() > 0) {
            this.queryTreeIntPreOrder(rootPath, "", "", queryElements, true, false, false, queriesOutput);
        }
    }

    /**
     * recursive function that constructs the full ZNode tree
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private void queryTreeIntPreOrder(String path, String indent, String name, List<ZKQueryElement> queryElements,
            boolean isQueryRoot, boolean isLast, boolean isParentLast, Hashtable<Integer, List<String>> queriesOutput)
            throws KeeperException, InterruptedException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        List<String> children = null;
        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        List<ACL> znodeACLList = this.zk.getACL(path, null);

        Boolean isQueryRootSentinel = true;
        // After we got the ACL, execute all the queries
        for (ZKQueryElement zkQueryElement : queryElements) {
            isQueryRootSentinel = false;
            String znodePrintColor = "";
            ZKQuery query = zkQueryElement.getQuery();

            if (query.query(znodeACLList, path, this.zk, zkQueryElement.getArgs())) {
                znodePrintColor = this.matchColor;
            } else {
                znodePrintColor = this.misMatchColor;
            }

            if (isQueryRoot) {
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
            queriesOutput.get(zkQueryElement.hashCode()).add(indent + znodePrintColor + "/" + name + this.resetColor);

        }

        if (path.equals("/")) {
            path = "";
        }
        Collections.sort(children);

        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            this.queryTreeIntPreOrder(path + "/" + child, indent, child, queryElements, isQueryRootSentinel,
                    !iterator.hasNext(), isLast, queriesOutput);
        }
    }

    /**
     * recursive function that constructs the full ZNode tree while passing parent
     * ACL to child queries
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private void queryTreeIntParentYesChildNo(String path, String indent, String name, List<ACL> parentACLList,
            boolean isQueryRoot, boolean isLast, boolean isParentLast, Hashtable<Integer, List<String>> queriesOutput,
            ZKQueryElement queryElement) throws KeeperException, InterruptedException, NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            return;
        }

        String znodePrintColor = "";

        ZKQuery query = queryElement.getQuery();

        if (parentACLList == null || query.query(parentACLList, path, this.zk, null)) {
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
        queriesOutput.get(queryElement.hashCode()).add(indent + this.resetColor + "/" + name);

        Collections.sort(children);

        Iterator<String> iterator = children.iterator();
        while (iterator.hasNext()) {
            String child = iterator.next();
            this.queryTreeIntParentYesChildNo(path + "/" + child, indent, child, parentACLList, false,
                    !iterator.hasNext(), isLast, queriesOutput, queryElement);
        }
    }

    /**
     * Function to return a list of the selected sub-tree with the full path of
     * query matching nodes
     *
     * @param rootPath      Path to start recursive query execution from
     * @param queryElements List of queries to be executed on each node
     * @param queriesOutput Output buffers for each query
     * @throws KeeperException
     * @throws InterruptedException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    public void queryFind(String rootPath, List<ZKQueryElement> queryElements,
            Hashtable<Integer, List<String>> queriesOutput)
            throws KeeperException, InterruptedException, NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {

        List<ZKQueryElement> invalidQueries = new ArrayList<ZKQueryElement>();
        List<ZKQueryElement> parentYesChildNoQueries = new ArrayList<ZKQueryElement>();
        for (ZKQueryElement zkQueryElement : queryElements) {
            // validate root path requested:
            try {
                if (this.zk.exists(zkQueryElement.getRootPath(), null) == null) {
                    queriesOutput.get(zkQueryElement.hashCode())
                            .add("The path " + zkQueryElement.getRootPath() + " does not exist.");
                    invalidQueries.add(zkQueryElement);
                    continue;
                }
            } catch (IllegalArgumentException e) {
                queriesOutput.get(zkQueryElement.hashCode())
                        .add("Invalid rootpath " + zkQueryElement.getRootPath() + " : " + e.getMessage());
                invalidQueries.add(zkQueryElement);
                continue;
            }

            if (zkQueryElement.getName().equals("parentYesChildNo")) {
                this.queryFindIntParentYesChildNo(zkQueryElement.getRootPath(), null, queriesOutput, zkQueryElement);
                parentYesChildNoQueries.add(zkQueryElement);
            }
        }

        for (ZKQueryElement zkQueryElement : parentYesChildNoQueries) {
            queryElements.remove(zkQueryElement);
        }

        for (ZKQueryElement zkQueryElement : invalidQueries) {
            queryElements.remove(zkQueryElement);
        }

        if (queryElements.size() > 0) {
            this.queryFindIntPreOrder(rootPath, queryElements, queriesOutput);
        }
    }

    /**
     * recursive function that constructs the full ZNode tree
     * 
     * @param queryElements List of queries to be executed on each node
     * @param queriesOutput Output buffers for each query
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private void queryFindIntPreOrder(String path, List<ZKQueryElement> queryElements,
            Hashtable<Integer, List<String>> queriesOutput) throws KeeperException, InterruptedException,
            NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            for (ZKQueryElement zkQueryElement : queryElements) {
                queriesOutput.get(zkQueryElement.hashCode())
                        .add("WARNING: No READ permission for " + path + ", skipping subtree");
            }
            return;
        }
        List<ACL> znodeACLList = this.zk.getACL(path, null);

        for (ZKQueryElement zkQueryElement : queryElements) {
            ZKQuery query = zkQueryElement.getQuery();

            if (query.query(znodeACLList, path, this.zk, zkQueryElement.getArgs())) {
                queriesOutput.get(zkQueryElement.hashCode()).add(path);
            }
        }

        if (path.equals("/")) {
            path = "";
        }

        Collections.sort(children);
        for (String child : children) {
            this.queryFindIntPreOrder(path + "/" + child, queryElements, queriesOutput);
        }
    }

    /**
     * recursive function that constructs the full ZNode tree, passing parent ACL to
     * children queries
     * 
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    private void queryFindIntParentYesChildNo(String path, List<ACL> parentACLList,
            Hashtable<Integer, List<String>> queriesOutput, ZKQueryElement queryElement)
            throws KeeperException, InterruptedException, NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {

        List<String> children = null;

        try {
            children = this.zk.getChildren(path, null);
        } catch (NoAuthException e) {
            queriesOutput.get(queryElement.hashCode())
                    .add("WARNING: No READ permission for " + path + ", skipping subtree");
            return;
        }

        ZKQuery query = queryElement.getQuery();

        if (parentACLList != null && !query.query(parentACLList, path, this.zk, queryElement.getArgs())) {
            queriesOutput.get(queryElement.hashCode()).add(path);
        }
        parentACLList = this.zk.getACL(path, null);

        if (path.equals("/")) {
            path = "";
        }

        Collections.sort(children);
        for (String child : children) {
            this.queryFindIntParentYesChildNo(path + "/" + child, parentACLList, queriesOutput, queryElement);
        }
    }

    /**
     * Return color code explanation for command line output
     * 
     * @return Hint for colors used in tree representation
     */
    public String colorCodeExplanation() {
        String explanation = "";
        explanation += "* " + this.matchColor + this.zk.getZKPConfig().getMatchColor() + this.resetColor
                + ": znodes matching the query" + "\n";
        explanation += "* " + this.misMatchColor + this.zk.getZKPConfig().getMismatchColor() + this.resetColor
                + ": znodes not matching the query" + "\n";
        return explanation;
    }
}