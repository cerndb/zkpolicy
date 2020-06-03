package ch.cern;

import lombok.EqualsAndHashCode;
import java.util.ArrayList;
import java.util.List;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

/**
 * Class that augments ZooKeeper ACL with field checking and comparison
 * functionality.
 */
@EqualsAndHashCode
public class ACLAugment {
  private ACL acl;

  /**
   * Construct ACLAugment from ACL object.
   *
   * @param initACL ZooKeeper ACL object.
   */
  public ACLAugment(ACL initACL) {
    this.acl = initACL;
  }

  /**
   * Construct ACLAugment from "scheme:id:permissions" string.
   *
   * @param stringACL String in scheme:id:permissions format
   */
  public ACLAugment(String stringACL) throws IllegalArgumentException {
    if (stringACL == null) {
      throw new IllegalArgumentException("Null stringACL provided");
    }

    long count = stringACL.chars().filter(ch -> ch == ':').count();
    if (count < 2 || count > 3) {
      throw new IllegalArgumentException(stringACL + " does not have the form scheme:id:perm");
    }

    String[] aclSegments = stringACL.split(":");

    if (!ZKPolicyDefs.Schemes.includes(aclSegments[0])) {
      throw new IllegalArgumentException(aclSegments[0] + " is not a valid scheme type");
    }

    ACL newAcl = new ACL();
    if (count == 3) {
      newAcl.setId(new Id(aclSegments[0], aclSegments[1] + ":" + aclSegments[2]));
      newAcl.setPerms(getPermFromString(aclSegments[3]));
    } else {
      newAcl.setId(new Id(aclSegments[0], aclSegments[1]));
      newAcl.setPerms(getPermFromString(aclSegments[2]));
    }

    this.acl = newAcl;
  }

  /**
   * Check if this ACL provides READ permission.
   *
   * @return True if ACL has the READ bit enabled.
   */
  public boolean hasRead() {
    return (this.acl.getPerms() & ZooDefs.Perms.READ) > 0;
  }

  /**
   * Check if this ACL provides WRITE permission.
   *
   * @return True if ACL has the WRITE bit enabled.
   */
  public boolean hasWrite() {
    return (this.acl.getPerms() & ZooDefs.Perms.WRITE) > 0;
  }

  /**
   * Check if this ACL provides CREATE permission.
   *
   * @return True if ACL has the CREATE bit enabled.
   */
  public boolean hasCreate() {
    return (this.acl.getPerms() & ZooDefs.Perms.CREATE) > 0;
  }

  /**
   * Check if this ACL provides DELETE permission.
   *
   * @return True if ACL has the DELETE bit enabled.
   */
  public boolean hasDelete() {
    return (this.acl.getPerms() & ZooDefs.Perms.DELETE) > 0;
  }

  /**
   * Check if this ACL provides ADMIN permission.
   *
   * @return True if ACL has the ADMIN bit enabled.
   */
  public boolean hasAdmin() {
    return (this.acl.getPerms() & ZooDefs.Perms.ADMIN) > 0;
  }

  /**
   * Get scheme field of ACL.
   *
   * @return Scheme of ACL.
   */
  public String getScheme() {
    return this.acl.getId().getScheme();
  }

  /**
   * Get id field of ACL.
   *
   * @return ID of ACL.
   */
  public String getId() {
    return this.acl.getId().getId();
  }

  /**
   * Get perm field of ACL.
   *
   * @return int permission representation of crwda bit sequence.
   */
  public int getPerms() {
    return this.acl.getPerms();
  }

  /**
   * Get the ACL object.
   *
   * @return ACL object encapsulated in the ACLAugment.
   */
  public ACL getACL() {
    return this.acl;
  }

  // From ZK 3.5.5 org.apache.zookeeper.cli.AclParser.java, not available on ZK
  // 3.4
  private static int getPermFromString(String permString) throws IllegalArgumentException {
    int perm = 0;
    for (int i = 0; i < permString.length(); i++) {
      switch (permString.charAt(i)) {
        case 'r':
          perm |= ZooDefs.Perms.READ;
          break;
        case 'w':
          perm |= ZooDefs.Perms.WRITE;
          break;
        case 'c':
          perm |= ZooDefs.Perms.CREATE;
          break;
        case 'd':
          perm |= ZooDefs.Perms.DELETE;
          break;
        case 'a':
          perm |= ZooDefs.Perms.ADMIN;
          break;
        default:
          throw new IllegalArgumentException("Unknown perm type: " + permString.charAt(i));
      }
    }
    return perm;
  }

  /**
   * Get ACL element in scheme:id:permissions String format.
   *
   * @return ACL element string in scheme:id:permissions format.
   */
  public String getStringFromACL() {
    String returnACL = getScheme() + ":" + getId() + ":";
    if (this.hasCreate()) {
      returnACL += "c";
    }
    if (this.hasDelete()) {
      returnACL += "d";
    }
    if (this.hasRead()) {
      returnACL += "r";
    }
    if (this.hasWrite()) {
      returnACL += "w";
    }
    if (this.hasAdmin()) {
      returnACL += "a";
    }
    return returnACL;
  }

  /**
   * Return List of ACLAugment elements from a list from ACL.
   * 
   * @param aclList ACL List from ZooKeeper
   * @return ACLAugment List
   */
  public static List<ACLAugment> generateACLAugmentList(List<ACL> aclList) {
    List<ACLAugment> toReturnList = new ArrayList<ACLAugment>();
    for (ACL acl : aclList) {
      toReturnList.add(new ACLAugment(acl));
    }
    return toReturnList;
  }

}
