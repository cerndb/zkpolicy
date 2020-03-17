package ch.cern;

import org.apache.zookeeper.data.ACL;

/**
 * Class that augments ZooKeeper ACL with field checking and comparison functionality
 */
public class ACLaugment {
    private  ACL acl ;
    
    /**
     * 
     * @param initACL ZooKeeper ACL object 
     */
    public ACLaugment(ACL initACL) {
        this.acl = initACL;
    }

    /**
     * Check if this ACL provides READ permission
     * @return True if ACL has the READ bit enabled
     */
    public boolean hasRead() {
        return (this.acl.getPerms() & 1) > 0;
    }

    /**
     * Check if this ACL provides WRITE permission
     * @return True if ACL has the WRITE bit enabled
     */
    public boolean hasWrite() {
        return (this.acl.getPerms() & 2) > 0;
    }

    /**
     * Check if this ACL provides CREATE permission
     * @return True if ACL has the CREATE bit enabled
     */
    public boolean hasCreate() {
        return (this.acl.getPerms() & 4) > 0;
    }

    /**
     * Check if this ACL provides DELETE permission
     * @return True if ACL has the DELETE bit enabled
     */
    public boolean hasDelete() {
        return (this.acl.getPerms() & 8) > 0;
    }

    /**
     * Check if this ACL provides ADMIN permission
     * @return True if ACL has the ADMIN bit enabled
     */
    public boolean hasAdmin() {
        return (this.acl.getPerms() & 16) > 0;
    }

    /**
     * Get scheme field of ACL
     * @return Scheme of ACL
     */
    public String getScheme() {
        return this.acl.getId().getScheme();
    }

    /**
     * Get id field of ACL
     * @return ID of ACL
     */
    public String getId() {
        return this.acl.getId().getId();
    }
   
}
