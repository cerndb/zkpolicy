package ch.cern;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * ZKDefaultQuery Class that provides a basic set of queries for the ZNode tree.
 */
public class ZKDefaultQuery {

    // Define default queries with appropriate names
    public exactACLDef exactACL = new exactACLDef();
    public noACLDef noACL = new noACLDef();
    public satisfyACLDef satisfyACL = new satisfyACLDef();
    public parentYesChildNoDef parentYesChildNo = new parentYesChildNoDef();
    public duplicateACLDef duplicateACL = new duplicateACLDef();
    public regexMatchACLDef regexMatchACL = new regexMatchACLDef();
    public globMatchACLDef globMatchACL = new globMatchACLDef();

    public ZKQuery getValueOf(String lookingForValue) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field field = this.getClass().getField(lookingForValue);
        return (ZKQuery)field.get(this);
    }

    /**
     * Match nodes that have equal ACL with the passed
     */
    private class exactACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLs) {
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
        }
    }

    /**
     * Match nodes that have no ACL restrictions (world:anyone:crwda)
     */
    private class noACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLs) {

            for (ACL aclElement : aclList) {
                ACLAugment ACLAugment = new ACLAugment(aclElement);
                if (ACLAugment.getScheme().equals("world") && ACLAugment.getId().equals("anyone")
                        && ACLAugment.hasCreate() && ACLAugment.hasWrite() && ACLAugment.hasAdmin()
                        && ACLAugment.hasRead() && ACLAugment.hasDelete()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Match nodes that do satisfy the passed ACL (logical match)
     */
    private class satisfyACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLs) {
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
        }
    }

    /**
     * Match nodes that wave ACL not complying with their parents ACL
     */
    private class parentYesChildNoDef implements ZKQuery {
        public boolean query(List<ACL> parentACLList, String path, ZooKeeper zk, String[] queryACLs) {
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
        }
    }

    /**
     * Match nodes that have no ACL restrictions (world:anyone:crwda)
     */
    private class duplicateACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLs) {
            HashSet<ACLAugment> unique = new HashSet<ACLAugment>();

            for (ACL aclElement : aclList) {
                ACLAugment ACLAugment = new ACLAugment(aclElement);
                if (!unique.add(ACLAugment)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Match nodes with ACLs matching the passed regular expressions
     */
    private class regexMatchACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLRegexList) {
            List<Pattern> queryPatternList = ZKPattern.createRegexPatternList(queryACLRegexList);

            int queryListSentinel = queryPatternList.size();
            for (ACL aclElement : aclList) {
                ACLAugment currentACLAugment = new ACLAugment(aclElement);
                String currentACLString = currentACLAugment.getStringFromACL();

                for (Pattern pattern : queryPatternList) {
                    Matcher currentMatcher = pattern.matcher(currentACLString);
                    if (currentMatcher.matches()) {
                        queryListSentinel--;
                    }
                    // If all regular expressions where matched, we can return true
                    if (queryListSentinel == 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Match nodes with ACLs matching the passed glob expressions
     */
    private class globMatchACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, String path, ZooKeeper zk, String[] queryACLGlobList) {
            List<Pattern> queryPatternList = ZKPattern.createGlobPatternList(queryACLGlobList);

            int queryListSentinel = queryPatternList.size();
            for (ACL aclElement : aclList) {
                ACLAugment currentACLAugment = new ACLAugment(aclElement);
                String currentACLString = currentACLAugment.getStringFromACL();

                for (Pattern pattern : queryPatternList) {
                    Matcher currentMatcher = pattern.matcher(currentACLString);
                    if (currentMatcher.matches()) {
                        queryListSentinel--;
                    }
                    // If all regular expressions where matched, we can return true
                    if (queryListSentinel == 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

}