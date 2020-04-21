package ch.cern;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

public class ZKEnforce {
    private ZooKeeper zk;
    private ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();

    public ZKEnforce(ZooKeeper zk) {
        this.zk = zk;
    }

    /**
     * Execute the query in dry-run mode returning the znodes to be altered from normal execution.
     * @param queryName Query to be recursively executed.
     * @param rootPath  Path to start recursive traversal from.
     * @param queryACLs Arguments for the query to be executed.
     * @throws NoSuchMethodException When the query provided is not implemented.
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws KeeperException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     */
    public void enforceDry(String queryName, String rootPath, String[] queryACLs)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, KeeperException, InterruptedException, NoSuchFieldException {
        
        ZKDefaultQuery zkDefaultQuery = new ZKDefaultQuery();
        ZKQuery query = zkDefaultQuery.getValueOf(queryName);
        enforceInnerDry(rootPath, query, queryACLs);
    }

    /**
     * Enforce policy ACLs provided to every matching node of the requested query.
     * @param policies ACLs to be enforced as policy to each matching node.
     * @param queryName Query to be recursively executed.
     * @param rootPath  Path to start recursive traversal from.
     * @param queryACLs Arguments for the query to be executed.
     * @param append Append policies to existing ACL definitions of each matching znode.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws KeeperException
     * @throws NoSuchFieldException
     */
    public void enforce(String[] policies, String queryName, String rootPath, String[] queryACLs, boolean append)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, InterruptedException, KeeperException, NoSuchFieldException {

        List<ACL> policiesACL = new ArrayList<ACL>();
        for (String policyACLString : policies) {
            policiesACL.add(new ACLAugment(policyACLString).getACL());
        }
        ZKQuery query = this.zkDefaultQuery.getValueOf(queryName);
        enforceInner(policiesACL, rootPath, query, queryACLs, append);
    }

    private void enforceInnerDry(String path, ZKQuery query, String[] queryACLs)
            throws InterruptedException, KeeperException {
        // Apply regex to path
        List<ACL> znodeACLList = null;
        List<String> children = null;
        
        // If the current user does not have READ permission for this znode, he cannot getACL or ls so just skip it
        try {
            znodeACLList = this.zk.getACL(path, null);
            children = this.zk.getChildren(path, null);
        } catch (KeeperException e) {
            System.out.println("WARNING: No READ permission for " + path + ", skipping subtree");
            return;
        }

        if (query.query(znodeACLList, path, zk, queryACLs)) {
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

    private void enforceInner(List<ACL> policies, String path, ZKQuery query, String[] queryACLs, boolean append)
            throws KeeperException, InterruptedException {
        // Apply regex to path
        List<ACL> znodeACLList = null;
        List<String> children = null;
        List<ACL> newACLList = new ArrayList<ACL>();

        // If the current user does not have READ permission for this znode, he cannot getACL or ls so just skip it
        try {
            znodeACLList = this.zk.getACL(path, null);
            children = this.zk.getChildren(path, null);
        } catch (KeeperException e) {
            System.out.println("WARNING: No READ permission for " + path + ", skipping subtree");
            return;
        }

        if (query.query(znodeACLList, path, zk, queryACLs)) {
            if (append) {
                newACLList.addAll(znodeACLList);
            }

            for (ACL policyACL : policies) {
                newACLList.add(policyACL);
            }
            // get here -P acls to add
            this.zk.setACL(path, newACLList, -1);
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