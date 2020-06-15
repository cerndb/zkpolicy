package ch.cern;

import org.apache.commons.collections4.Equator;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

/**
 * Equator used for logical matching of ACLs.
 */
public class ACLAugmentSatisfyEquator implements Equator<ACLAugment> {

  // Check if o2 ACL logically satisfies o1 ACL
  @Override
  public boolean equate(ACLAugment o1, ACLAugment o2) {
    boolean check_id;
    boolean check_scheme;
    boolean check_perms;
    // world:anyone is the most permissive scheme:id combination so only
    // check if permissions are logically satisfied
    check_perms =  (o1.getPerms() & o2.getPerms()) == o1.getPerms();
    if (o2.getScheme().equals("world") && o2.getId().equals("anyone")) {
      check_scheme = true;
      check_id = true;
    } else if (o1.getScheme().equals("ip") && o2.getScheme().equals("ip") && ZKPolicyUtils.isIPV4SubnetAddress(o2.getId())){
      // Validate that o1 is a valid IPv4 address
      if (!ZKPolicyUtils.isIPV4Address(o1.getId())) {
        throw new IllegalArgumentException(o1.getId() + " is not a valid IPv4 address");
      }
      SubnetInfo subnet = new SubnetUtils(o2.getId()).getInfo();
      check_scheme = true;
      check_id = subnet.isInRange(o1.getId());
    } else {
      check_scheme = o1.getScheme().equals(o2.getScheme());
      check_id = o1.getId().equals(o2.getId());
    }
    return check_scheme && check_id && check_perms;
  }

  @Override
  public int hash(ACLAugment o1) {
    return o1.hashCode();
  }

}