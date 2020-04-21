package ch.cern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ACLAugmentEquatorTest {
    TestingServer zkTestServer;
    CuratorFramework cli;

    @Test
    public void testEquate() {
        ACLAugmentEquator equator = new ACLAugmentEquator();
        ACLAugment o1 = new ACLAugment("sasl:test:crwda");
        ACLAugment o2 = new ACLAugment("world:anyone:crw");

        assertTrue(equator.equate(o1, o2));

        o1 = new ACLAugment("sasl:test:crw");
        o2 = new ACLAugment("world:anyone:da");

        assertFalse(equator.equate(o1, o2));

        o1 = new ACLAugment("sasl:test:crw");
        o2 = new ACLAugment("world:test:da");

        assertFalse(equator.equate(o1, o2));

        o1 = new ACLAugment("sasl:test:crw");
        o2 = new ACLAugment("ip:anyone:crw");

        assertFalse(equator.equate(o1, o2));

    }

    @Test
    public void testComplementaryEquate() {
        ACLAugmentEquator equator = new ACLAugmentEquator();
        ACLAugment o1 = new ACLAugment("sasl:test:crw");
        ACLAugment o2 = new ACLAugment("world:test:da");

        assertFalse(equator.equate(o1, o2));

        o1 = new ACLAugment("sasl:test:crw");
        o2 = new ACLAugment("ip:anyone:crw");

        assertFalse(equator.equate(o1, o2));

    }

    @Test
    public void testNonWorldAnyoneEquate() {
        ACLAugmentEquator equator = new ACLAugmentEquator();
        ACLAugment o1 = new ACLAugment("sasl:test:crw");
        ACLAugment o2 = new ACLAugment("ip:anyone:crw");

        assertFalse(equator.equate(o1, o2));

    }
}
