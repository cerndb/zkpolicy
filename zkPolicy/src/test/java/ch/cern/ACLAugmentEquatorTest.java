package ch.cern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    ACLAugmentSatisfyEquator equator = new ACLAugmentSatisfyEquator();
    ACLAugment o1 = new ACLAugment("sasl:test:crwda");
    ACLAugment o2 = new ACLAugment("world:anyone:crw");

    assertFalse(equator.equate(o1, o2));
    assertFalse(equator.equate(o2, o1));

    o1 = new ACLAugment("sasl:test:crw");
    o2 = new ACLAugment("world:anyone:da");

    assertFalse(equator.equate(o1, o2));

    o1 = new ACLAugment("sasl:test:crw");
    o2 = new ACLAugment("world:test:da");

    assertFalse(equator.equate(o1, o2));

    o1 = new ACLAugment("sasl:test:crw");
    o2 = new ACLAugment("ip:0.0.0.0:crw");

    assertFalse(equator.equate(o1, o2));

    o1 = new ACLAugment("world:anyone:crw");
    o2 = new ACLAugment("sasl:test:c");

    assertTrue(equator.equate(o2, o1));
  }

  @Test
  public void testComplementaryEquate() {
    ACLAugmentSatisfyEquator equator = new ACLAugmentSatisfyEquator();
    ACLAugment o1 = new ACLAugment("sasl:test:crw");
    ACLAugment o2 = new ACLAugment("world:test:da");

    assertFalse(equator.equate(o1, o2));

    o1 = new ACLAugment("sasl:test:crw");
    o2 = new ACLAugment("ip:0.0.0.0:crw");

    assertFalse(equator.equate(o1, o2));
  }

  @Test
  public void testSubnetEquate() {
    ACLAugmentSatisfyEquator equator = new ACLAugmentSatisfyEquator();
    ACLAugment o1 = new ACLAugment("ip:127.0.0.4:d");
    ACLAugment o2 = new ACLAugment("ip:127.0.0.0/24:cdra");

    assertTrue(equator.equate(o1, o2));

    o1 = new ACLAugment("ip:127.0.1.4:d");
    o2 = new ACLAugment("ip:127.0.0.0/24:cdra");

    assertFalse(equator.equate(o1, o2));

    o1 = new ACLAugment("ip:127.0.0.0:d");
    o2 = new ACLAugment("ip:127.0.0.0/24:cdra");

    assertTrue(equator.equate(o1, o2));

    assertThrows(IllegalArgumentException.class, () -> {
      equator.equate(new ACLAugment("ip:127.0.0a.0:d"), new ACLAugment("ip:127.0.0.0/24:cdra"));
    });
  }

  @Test
  public void testNonWorldAnyoneEquate() {
    ACLAugmentSatisfyEquator equator = new ACLAugmentSatisfyEquator();
    ACLAugment o1 = new ACLAugment("sasl:test:crw");
    ACLAugment o2 = new ACLAugment("ip:0.0.0.0:crw");

    assertFalse(equator.equate(o1, o2));
  }
}
