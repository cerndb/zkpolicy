package ch.cern;

import org.apache.commons.collections4.Equator;

/**
 * Equator used for logical matching of ACLs.
 */
public class ACLAugmentEquator implements Equator<ACLAugment> {

    // Check if o2 ACL logically satisfies o1 ACL
    @Override
    public boolean equate(ACLAugment o1, ACLAugment o2) {
        boolean check_id;
        boolean check_scheme;
        boolean check_perms;
        // world:anyone is the most permissive scheme:id combination so only
        // check if permissions are logically satisfied
        if (o2.getScheme().equals("world") && o2.getId().equals("anyone")) {
            check_perms = ((o1.getPerms() & o2.getPerms()) == o1.getPerms())
                    || ((o1.getPerms() & o2.getPerms()) == o2.getPerms());
            check_scheme = true;
            check_id = true;
        } else {
            check_scheme = o1.getScheme().equals(o2.getScheme());
            check_id = o1.getId().equals(o2.getId());
            check_perms = ((o1.getPerms() & o2.getPerms()) == o1.getPerms())
                    || ((o1.getPerms() & o2.getPerms()) == o2.getPerms());
        }
        return check_scheme && check_id && check_perms;
    }

    @Override
    public int hash(ACLAugment o1) {
        return o1.hashCode();
    }

}