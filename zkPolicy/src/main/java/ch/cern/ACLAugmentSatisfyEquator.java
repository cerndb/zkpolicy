package ch.cern;

import org.apache.commons.collections4.Equator;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

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
    check_perms = (o1.getPerms() & o2.getPerms()) == o1.getPerms();
    if (o2.getScheme().equals("world") && o2.getId().equals("anyone")) {
      check_scheme = true;
      check_id = true;
    } else if (o1.getScheme().equals("ip") && o2.getScheme().equals("ip")) {
      check_scheme = true;
      IPAddressString addrStringOperand2 = new IPAddressString(o2.getId());
      try {
        IPAddress addrOperand2 = addrStringOperand2.getAddress();
        Integer prefix = addrOperand2.getNetworkPrefixLength();
        if (prefix == null) {
          // o2 is not a subnet address
          check_id = o1.getId().equals(o2.getId());
        } else {
          IPAddressString addrStringOperand1 = new IPAddressString(o1.getId());
          IPAddress addrOperand1 = addrStringOperand1.toAddress();
          check_id = addrOperand2.contains(addrOperand1);
        }
      } catch (AddressStringException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
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