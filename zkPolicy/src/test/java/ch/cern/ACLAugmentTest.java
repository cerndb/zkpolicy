package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ACLAugmentTest {
    TestingServer zkTestServer;
    CuratorFramework cli;

    @Test
    public void testConstructWithString() throws Exception {
        ACLAugment testACLAugment = new ACLAugment("digest:user1:passw1:crw");
        assertEquals("digest",testACLAugment.getScheme());
        assertEquals("user1:passw1",testACLAugment.getId());
        assertEquals(0b00111,testACLAugment.getPerms());
    }

    @Test
    public void testConstructWithNull() throws Exception {
        try {
            String stringACL = null;
            new ACLAugment(stringACL);
        } catch (IllegalArgumentException e) {
            assertEquals("Null stringACL provided", e.getMessage());
        }
    }

    @Test
    public void testHasRead() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00001, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertTrue(testACLAugment.hasRead());
    }

    @Test
    public void testNotHasRead() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertFalse(testACLAugment.hasRead());
    }

    @Test
    public void testHasWrite() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00010, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertTrue(testACLAugment.hasWrite());
    }

    @Test
    public void testNotHasWrite() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertFalse(testACLAugment.hasWrite());
    }

    @Test
    public void testHasCreate() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00100, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertTrue(testACLAugment.hasCreate());
    }

    @Test
    public void testNotHasCreate() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertFalse(testACLAugment.hasCreate());
    }

    @Test
    public void testHasDelete() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b01000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertTrue(testACLAugment.hasDelete());
    }

    @Test
    public void testNotHasDelete() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertFalse(testACLAugment.hasDelete());
    }

    @Test
    public void testHasAdmin() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b10000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertTrue(testACLAugment.hasAdmin());
    }

    @Test
    public void testNotHasAdmin() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertFalse(testACLAugment.hasAdmin());
    }

    @Test
    public void testGetId() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertEquals("testuser", testACLAugment.getId());
    }

    @Test
    public void testGetScheme() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertEquals("digest", testACLAugment.getScheme());
    }

    @Test
    public void testGetACL() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLAugment testACLAugment = new ACLAugment(zkACL);
        assertEquals(zkACL.toString(), testACLAugment.getACL().toString());
    }

    @Test
    public void testGetPerms() throws Exception {
        ACLAugment testACLAugment = new ACLAugment("world:anyone:crwda");
        assertEquals(0b11111, testACLAugment.getPerms());
    }

    @Test
    public void testEquals() throws Exception {
        ACLAugment testObject1 = new ACLAugment("digest:user1:passw1:crw");
        Id zkId = new Id("digest", "user1:passw1");
        ACL zkACL = new ACL(0b00111, zkId);
        ACLAugment testObject2 = new ACLAugment(zkACL);
        assertTrue(testObject1.equals(testObject2));
    }

    @Test
    public void testNotEquals() throws Exception {
        ACLAugment testObject1 = new ACLAugment("world:testuser:crd");
        Id zkId = new Id("test", "testuser");
        ACL zkACL = new ACL(0b01101, zkId);
        ACLAugment testObject2 = new ACLAugment(zkACL);
        assertFalse(testObject1.equals(testObject2));
    }

    @Test
    public void testEqualsDifferentObjects() throws Exception {
        ACLAugment testObject1 = new ACLAugment("world:testuser:crd");
        assertFalse(testObject1.equals(null));
    }

    @Test
    public void testHashCode() throws Exception {
        ACLAugment testObject1 = new ACLAugment("sasl:testprincipal:cd");
        Id zkId = new Id("sasl", "testprincipal");
        ACL zkACL = new ACL(0b01100, zkId);
        ACLAugment testObject2 = new ACLAugment(zkACL);
        assertEquals(testObject1.hashCode(), testObject2.hashCode());
    }

    @Test
    public void testInvalidACLString() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {new ACLAugment("invalid:acl:crwd");});
        assertThrows(IllegalArgumentException.class, () -> {new ACLAugment("invalid:acl");});
        assertThrows(IllegalArgumentException.class, () -> {new ACLAugment("nocolon");});
    }

    @Test
    public void testInvalidACLPermissionChar() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {new ACLAugment("sasl:test:crwf");});
    }

    @Test
    public void testGetStringFromACL() throws Exception {
        ACLAugment aclAugment = new ACLAugment("world:anyone:cdrwa");
        assertEquals("world:anyone:cdrwa", aclAugment.getStringFromACL());
        aclAugment = new ACLAugment("world:anyone:cdrw");
        assertEquals("world:anyone:cdrw", aclAugment.getStringFromACL());
    }

}
