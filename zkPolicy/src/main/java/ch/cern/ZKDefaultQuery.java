package ch.cern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.ACL;

/**
 * ZKDefaultQuery Class that provides a basic set of queries for the ZNode tree.
 */
public class ZKDefaultQuery {

    public ZKQuery exactACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {
            // If the two provided lists do not have the same length, we are sure that the
            // query should fail
            if (aclList.size() != queryACLs.length) {
                return false;
            }

            // construct the string ACL list of current checked element
            List<String> aclListString = new ArrayList<String>();
            for (ACL aclElement : aclList) {
                aclListString.add(aclElement.toString());
            }

            // Get sublist removing the first argument (name of query)
            List<String> queryACLsSub = Arrays.asList(queryACLs);
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

    public ZKQuery satisfyACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {
            List<ACLAugment> aclListAugment = new ArrayList<ACLAugment>();

            for (ACL aclElement : aclList) {
                aclListAugment.add(new ACLAugment(aclElement));
            }

            // queryACL list
            for (String queryACLString : queryACLs) {
                ACLAugment temp = new ACLAugment(queryACLString);
                if (IterableUtils.contains(aclListAugment, temp, new ACLAugmentEquator())) {
                    continue;
                }
                return false;
            }

            return true;
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
     * Select all nodes that have no ACL restrictions (world:anyone:crwda)
     *
     * @return ZKQuery object
     */
    public ZKQuery duplicateACL() {
        ZKQuery query = (aclList, path, zk, queryACLs) -> {

            HashSet<ACLAugment> unique = new HashSet<ACLAugment>();

            for (ACL aclElement : aclList) {
                ACLAugment ACLAugment = new ACLAugment(aclElement);
                if (!unique.add(ACLAugment)) {
                    return true;
                }
            }
            return false;
        };
        return query;
    }

}