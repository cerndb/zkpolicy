package ch.cern;

import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import lombok.EqualsAndHashCode;

/**
 * Class that augments ZooKeeper ACL with field checking and comparison
 * functionality
 */
@EqualsAndHashCode
public class ACLAugment {
    private ACL acl;

    /**
     * Construct ACLAugment from ACL object
     *
     * @param initACL ZooKeeper ACL object
     */
    public ACLAugment(ACL initACL) {
        this.acl = initACL;
    }

    /**
     * Construct ACLAugment from "scheme:id:permissions" string
     *
     * @param stringACL String in scheme:id:permissions format
     */
    public ACLAugment(String stringACL) throws IllegalArgumentException {
        Id id;
        int perms = 0;
        String idString = "";
        String permString = "";
        if (stringACL == null) {
            throw new IllegalArgumentException("Null stringACL provided");
        }
        String[] stringParts = stringACL.split(":");
        // stringParts = scheme:id:permissions
        switch (stringParts[0]) {
            case "world":
            case "ip":
                idString = stringParts[1];
                permString = stringParts[2];
                break;
            case "digest":
            case "auth":
                idString = stringParts[1] + ":" + stringParts[2];
                permString = stringParts[3];
                break;
            default:
                break;
        }
        id = new Id(stringParts[0], idString);
        if (permString.contains("r")) {
            perms += 0b00001;
        }
        if (permString.contains("w")) {
            perms += 0b00010;
        }
        if (permString.contains("c")) {
            perms += 0b00100;
        }
        if (permString.contains("d")) {
            perms += 0b01000;
        }
        if (permString.contains("a")) {
            perms += 0b10000;
        }
        this.acl = new ACL(perms, id);
    }

    /**
     * Check if this ACL provides READ permission
     *
     * @return True if ACL has the READ bit enabled
     */
    public boolean hasRead() {
        return (this.acl.getPerms() & 1) > 0;
    }

    /**
     * Check if this ACL provides WRITE permission
     *
     * @return True if ACL has the WRITE bit enabled
     */
    public boolean hasWrite() {
        return (this.acl.getPerms() & 2) > 0;
    }

    /**
     * Check if this ACL provides CREATE permission
     *
     * @return True if ACL has the CREATE bit enabled
     */
    public boolean hasCreate() {
        return (this.acl.getPerms() & 4) > 0;
    }

    /**
     * Check if this ACL provides DELETE permission
     *
     * @return True if ACL has the DELETE bit enabled
     */
    public boolean hasDelete() {
        return (this.acl.getPerms() & 8) > 0;
    }

    /**
     * Check if this ACL provides ADMIN permission
     *
     * @return True if ACL has the ADMIN bit enabled
     */
    public boolean hasAdmin() {
        return (this.acl.getPerms() & 16) > 0;
    }

    /**
     * Get scheme field of ACL
     *
     * @return Scheme of ACL
     */
    public String getScheme() {
        return this.acl.getId().getScheme();
    }

    /**
     * Get id field of ACL
     *
     * @return ID of ACL
     */
    public String getId() {
        return this.acl.getId().getId();
    }

    /**
     * Get perm field of ACL
     *
     * @return int permission representation of crwda bit sequence
     */
    public int getPerms() {
        return this.acl.getPerms();
    }

    /**
     * Get the ACL object
     *
     * @return ACL object encapsulated in the ACLAugment
     */
    public ACL getACL() {
        return this.acl;
    }
}
