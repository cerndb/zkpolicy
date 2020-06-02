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
import org.apache.zookeeper.data.ACL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ZKDefaultQuery Class that provides a basic set of queries for the ZNode tree.
 */
public class ZKDefaultQuery {
    private static Logger logger = LogManager.getLogger(ZKDefaultQuery.class);

    // Define default queries with appropriate names
    public exactACLDef exactACL = new exactACLDef();
    public noACLDef noACL = new noACLDef();
    public satisfyACLDef satisfyACL = new satisfyACLDef();
    public parentYesChildNoDef parentYesChildNo = new parentYesChildNoDef();
    public duplicateACLDef duplicateACL = new duplicateACLDef();
    public regexMatchACLDef regexMatchACL = new regexMatchACLDef();
    public globMatchACLDef globMatchACL = new globMatchACLDef();
    public globMatchPathDef globMatchPath = new globMatchPathDef();
    public regexMatchPathDef regexMatchPath = new regexMatchPathDef();

    /**
     * Get value of class field using its name
     * 
     * @param lookingForValue Field name
     * @return Corresponding query
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public ZKQuery getValueOf(String lookingForValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = this.getClass().getField(lookingForValue);
        return (ZKQuery) field.get(this);
    }

    /**
     * Match nodes that have equal ACL with the passed
     */
    private class exactACLDef implements ZKQuery {
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, String[] queryACLs) {
            // If the two provided lists do not have the same length, we are sure that the
            // query should fail
            if (aclList == null || aclList.size() != queryACLs.length) {
                return false;
            }

            List<ACLAugment> aclListAugment = ACLAugment.generateACLAugmentList(aclList);

            // Get sublist removing the first argument (name of query)
            List<String> queryACLsSub = Arrays.asList(queryACLs);
            List<ACLAugment> queryACLString = new ArrayList<ACLAugment>();
            for (String queryACL : queryACLsSub) {
                ACLAugment tempACLAugment = new ACLAugment(queryACL);
                queryACLString.add(tempACLAugment);
            }

            if (aclListAugment.containsAll(queryACLString)) {
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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, String[] queryACLs) {

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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, String[] queryACLs) {
            List<ACLAugment> aclListAugment = ACLAugment.generateACLAugmentList(aclList);

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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, String[] queryACLs) {
            List<ACLAugment> myACLsAugment = null;
            List<ACLAugment> parentACLsAugment = null;
            myACLsAugment = ACLAugment.generateACLAugmentList(aclList);
            parentACLsAugment = ACLAugment.generateACLAugmentList(parentAclList);

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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, String[] queryACLs) {
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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
                String[] queryACLRegexList) {
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
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
                String[] queryACLGlobList) {
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

    /**
     * Match nodes with ACLs matching the passed glob expressions
     */
    private class globMatchPathDef implements ZKQuery {
        // We do expect only one glob expression to check for path match
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
                String[] queryPathGlobList) {
            List<Pattern> queryPatternList = ZKPattern.createGlobPatternList(queryPathGlobList);

            Matcher pathMatcher = queryPatternList.get(0).matcher(path);
            if (pathMatcher.matches()) {
                return true;
            }
            return false;
        }
    }

    /**
     * Match nodes with ACLs matching the passed glob expressions
     */
    private class regexMatchPathDef implements ZKQuery {
        // We do expect only one glob expression to check for path match
        public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
                String[] queryPathRegexList) {
            List<Pattern> queryPatternList = ZKPattern.createRegexPatternList(queryPathRegexList);
            Matcher pathMatcher = queryPatternList.get(0).matcher(path);
            if (pathMatcher.matches()) {
                return true;
            }
            return false;
        }
    }

}