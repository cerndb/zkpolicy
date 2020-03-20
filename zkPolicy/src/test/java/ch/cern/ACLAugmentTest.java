package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void testHasRead() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00001, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertTrue(testAcLaugment.hasRead());
    }

    @Test
    public void testNotHasRead() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertFalse(testAcLaugment.hasRead());
    }

    @Test
    public void testHasWrite() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00010, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertTrue(testAcLaugment.hasWrite());
    }

    @Test
    public void testNotHasWrite() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertFalse(testAcLaugment.hasWrite());
    }

    @Test
    public void testHasCreate() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00100, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertTrue(testAcLaugment.hasCreate());
    }

    @Test
    public void testNotHasCreate() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertFalse(testAcLaugment.hasCreate());
    }

    @Test
    public void testHasDelete() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b01000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertTrue(testAcLaugment.hasDelete());
    }

    @Test
    public void testNotHasDelete() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertFalse(testAcLaugment.hasDelete());
    }

    @Test
    public void testHasAdmin() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b10000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertTrue(testAcLaugment.hasAdmin());
    }

    @Test
    public void testNotHasAdmin() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertFalse(testAcLaugment.hasAdmin());
    }

    @Test
    public void testGetId() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertEquals("testuser", testAcLaugment.getId());
    }

    @Test
    public void testGetScheme() throws Exception {
        Id zkId = new Id("digest", "testuser");
        ACL zkACL = new ACL(0b00000, zkId);
        ACLaugment testAcLaugment = new ACLaugment(zkACL);
        assertEquals("digest", testAcLaugment.getScheme());
    }

}
