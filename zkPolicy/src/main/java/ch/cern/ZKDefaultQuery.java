package ch.cern;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.zookeeper.data.ACL;
import ch.cern.ZKPolicyDefs.Queries;

/**
 * ZKDefaultQuery Class that provides a basic set of queries for the ZNode tree.
 */
public class ZKDefaultQuery {

  // Define default queries with appropriate names
  public ExactACLDef exactACL = new ExactACLDef();
  public NoACLDef noACL = new NoACLDef();
  public SatisfyACLDef satisfyACL = new SatisfyACLDef();
  public NoSatisfyACLDef noSatisfyACL = new NoSatisfyACLDef();
  public ParentYesChildNoDef parentYesChildNo = new ParentYesChildNoDef();
  public DuplicateACLDef duplicateACL = new DuplicateACLDef();
  public RegexMatchACLDef regexMatchACL = new RegexMatchACLDef();
  public GlobMatchACLDef globMatchACL = new GlobMatchACLDef();
  public GlobMatchPathDef globMatchPath = new GlobMatchPathDef();
  public RegexMatchPathDef regexMatchPath = new RegexMatchPathDef();

  /**
   * Get value of class field using its name.
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
   * Match znodes with ACL equal to the passed ACL argument.
   */
  private static class ExactACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.EXACT_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      // If the two provided lists do not have the same length, we are sure that the
      // query should fail
      if (aclList == null || aclList.size() != queryACLs.size()) {
        return false;
      }

      List<ACLAugment> aclListAugment = ACLAugment.generateACLAugmentList(aclList);

      // Get sublist removing the first argument (name of query)
      List<String> queryACLsSub = queryACLs;
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
   * Match znodes with no access control restrictions (ACL equals [world:anyone:cdrwa]).
   */
  private static class NoACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.NO_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      for (ACL aclElement : aclList) {
        ACLAugment aclAugment = new ACLAugment(aclElement);
        if (aclAugment.getScheme().equals("world") && aclAugment.getId().equals("anyone") && aclAugment.hasCreate()
            && aclAugment.hasWrite() && aclAugment.hasAdmin() && aclAugment.hasRead() && aclAugment.hasDelete()) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Match znodes are accessible by clients with the passed authentication info arguments (logical match).
   */
  private static class SatisfyACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.SATISFY_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      List<ACLAugment> aclListAugment = ACLAugment.generateACLAugmentList(aclList);

      // queryACL list
      for (String queryACLString : queryACLs) {
        ACLAugment temp = new ACLAugment(queryACLString);
        if (IterableUtils.contains(aclListAugment, temp, new ACLAugmentSatisfyEquator())) {
          continue;
        } else {
          return false;
        }
      }

      return true;
    }
  }

  /**
   * Match znodes that are not accessible by clients with the passed authentication info arguments.
   */
  private static class NoSatisfyACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.NO_SATISFY_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      List<ACLAugment> aclListAugment = ACLAugment.generateACLAugmentList(aclList);

      // queryACL list
      for (String queryACLString : queryACLs) {
        ACLAugment temp = new ACLAugment(queryACLString);
        if (IterableUtils.contains(aclListAugment, temp, new ACLAugmentSatisfyEquator())) {
          continue;
        } else {
          return true;
        }
      }
      return false;
    }
  }
  /**
   * Match znodes with ACL not equal to their parent's ACL.
   */
  private static class ParentYesChildNoDef implements ZKQuery {
    public String getDescription(){
      return Queries.PARENT_YES_CHILD_NO_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
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
   * Match znodes with duplicate ACL entries.
   */
  private static class DuplicateACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.DUPLICATE_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryACLs) {
      HashSet<ACLAugment> unique = new HashSet<ACLAugment>();

      for (ACL aclElement : aclList) {
        ACLAugment aclAugment = new ACLAugment(aclElement);
        if (!unique.add(aclAugment)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Match znodes with ACL entries matching the passed regular expression arguments.
   */
  private static class RegexMatchACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.REGEX_MATCH_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
        List<String> queryACLRegexList) {
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
   * Match znodes with ACL entries matching the passed glob pattern arguments.
   */
  private static class GlobMatchACLDef implements ZKQuery {
    public String getDescription(){
      return Queries.GLOB_MATCH_ACL_DESCRIPTION;
    }

    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
        List<String> queryACLGlobList) {
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
   * Match znodes with paths matching the passed glob pattern argument.
   */
  private static class GlobMatchPathDef implements ZKQuery {
    public String getDescription(){
      return Queries.GLOB_MATCH_PATH_DESCRIPTION;
    }

    // We expect only one glob expression to check for path match
    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
        List<String> queryPathGlobList) {
      List<Pattern> queryPatternList = ZKPattern.createGlobPatternList(queryPathGlobList);

      Matcher pathMatcher = queryPatternList.get(0).matcher(path);
      if (pathMatcher.matches()) {
        return true;
      }
      return false;
    }
  }

  /**
   * Match znodes with paths matching the passed regular expression argument.
   */
  private static class RegexMatchPathDef implements ZKQuery {
    public String getDescription(){
      return Queries.REGEX_MATCH_PATH_DESCRIPTION;
    }

    // We do expect only one glob expression to check for path match
    public boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk,
        List<String> queryPathRegexList) {
      List<Pattern> queryPatternList = ZKPattern.createRegexPatternList(queryPathRegexList);
      Matcher pathMatcher = queryPatternList.get(0).matcher(path);
      if (pathMatcher.matches()) {
        return true;
      }
      return false;
    }
  }

}