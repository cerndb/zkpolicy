package ch.cern;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

/**
 * ZKDefaultQuery Class that provides a basic set of queries for the ZNode tree
 */
public class ZKDefaultQuery {

    private Hashtable<String, String> methodDescriptions = new Hashtable<String, String>();

    public ZKDefaultQuery() {
        methodDescriptions.put("noACL", "Nodes without ACL definitions (world:anyone:crwda)");
        methodDescriptions.put("exportAll", "All nodes accessible by user");
        methodDescriptions.put("listDefaults", "Print the list of default queries");
        methodDescriptions.put("partialACL", "<arg1...argn> Nodes satisfying all of the provided arg ACLs");
        methodDescriptions.put("exactACL", "<arg1...argn> Nodes exclusively satisfying all of the provided arg ACLs");
        methodDescriptions.put("parentYesChildNo", "Nodes that do not align with their parent's ACL");
    }

    public ZKQuery exactACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {
            // If the two provided lists do not have the same length, we are sure that the
            // query should fail
            if (aclList.size() != queryACLs.length - 1) {
                return false;
            }

            // construct the string ACL list of current checked element
            List<String> aclListString = new ArrayList<String>();
            for (ACL aclElement : aclList) {
                aclListString.add(aclElement.toString());
            }

            // Get sublist removing the first argument (name of query)
            List<String> queryACLsSub = Arrays.asList(queryACLs).subList(1, queryACLs.length);
            List<String> queryACLString = new ArrayList<String>();
            for (String queryACL : queryACLsSub) {
                ACLAugment tempACLAugment = new ACLAugment(queryACL);
                queryACLString.add(tempACLAugment.getACL().toString());
            }

            if (aclListString.containsAll(queryACLString)) {
                return true;
            } else {
                return false;
            }
        };
        return query;
    }

    /**
     * Query that is satisfied by all tree znodes
     *
     * @return ZKQuery object
     */
    public ZKQuery exportAll() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {
            return true;
        };
        return query;
    }

    /**
     * Select all nodes that have no ACL restrictions (world:anyone:crwda)
     *
     * @return ZKQuery object
     */
    public ZKQuery noACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {

            for (ACL aclElement : aclList) {
                ACLAugment ACLAugment = new ACLAugment(aclElement);
                if (ACLAugment.getScheme().equals("world") && ACLAugment.getId().equals("anyone")
                        && ACLAugment.hasCreate() && ACLAugment.hasWrite() && ACLAugment.hasAdmin()
                        && ACLAugment.hasRead() && ACLAugment.hasDelete()) {
                    return true;
                }
            }
            return false;
        };
        return query;
    }

    public ZKQuery partialACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {
            // construct the string ACL list of current checked element
            List<String> aclListString = new ArrayList<String>();
            for (ACL aclElement : aclList) {
                aclListString.add(aclElement.toString());
            }

            // Get sublist removing the first argument (name of query)
            List<String> queryACLsSub = Arrays.asList(queryACLs).subList(1, queryACLs.length);
            List<String> queryACLString = new ArrayList<String>();
            for (String queryACL : queryACLsSub) {
                ACLAugment tempACLAugment = new ACLAugment(queryACL);
                queryACLString.add(tempACLAugment.getACL().toString());
            }

            if (aclListString.containsAll(queryACLString)) {
                return true;
            } else {
                return false;
            }
        };
        return query;
    }

    public ZKQuery parentYesChildNo() {
        ZKQuery query = (parentACLList, path, zk, queryACLs) -> {
            List<ACL> myACLs;
            List<String> myACLsAugment = new ArrayList<String>();
            List<String> parentACLsAugment = new ArrayList<String>();

            try {
                myACLs = zk.getACL(path, null);

                for (ACL myACL : myACLs) {
                    myACLsAugment.add(new ACLAugment(myACL).getACL().toString());
                }

                for (ACL parentACL : parentACLList) {
                    parentACLsAugment.add(new ACLAugment(parentACL).getACL().toString());
                }

            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }

            if (CollectionUtils.isEqualCollection(myACLsAugment, parentACLsAugment)) {
                return true;
            }
            return false;

        };
        return query;
    }

    /**
     * List default queries provided by the tool.
     */
    public void listDefaults() {
        System.out.println("Default queries list:");
        try {
            Class<?> thisClass = this.getClass();
            Method[] methods = thisClass.getDeclaredMethods();

            // We print all methods but those named lambda$*
            for (Method method : methods) {
                String methodName = method.getName();
                if (!methodName.startsWith("lambda$")) {
                    System.out.printf("* %-30.30s  %-30.70s%n", methodName, methodDescriptions.get(methodName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}