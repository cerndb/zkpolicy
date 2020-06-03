package ch.cern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKPolicyDefsTest {

  @Test
  public void testColorsEnum() {

    assertAll(() -> assertEquals("BLACK", ZKPolicyDefs.Colors.BLACK.name()),
        () -> assertEquals("\u001B[30m", ZKPolicyDefs.Colors.BLACK.getANSIValue()),

        () -> assertEquals("RED", ZKPolicyDefs.Colors.RED.name()),
        () -> assertEquals("\u001B[31m", ZKPolicyDefs.Colors.RED.getANSIValue()),

        () -> assertEquals("GREEN", ZKPolicyDefs.Colors.GREEN.name()),
        () -> assertEquals("\u001B[32m", ZKPolicyDefs.Colors.GREEN.getANSIValue()),

        () -> assertEquals("YELLOW", ZKPolicyDefs.Colors.YELLOW.name()),
        () -> assertEquals("\u001B[33m", ZKPolicyDefs.Colors.YELLOW.getANSIValue()),

        () -> assertEquals("BLUE", ZKPolicyDefs.Colors.BLUE.name()),
        () -> assertEquals("\u001B[34m", ZKPolicyDefs.Colors.BLUE.getANSIValue()),

        () -> assertEquals("MAGENTA", ZKPolicyDefs.Colors.MAGENTA.name()),
        () -> assertEquals("\u001B[35m", ZKPolicyDefs.Colors.MAGENTA.getANSIValue()),

        () -> assertEquals("CYAN", ZKPolicyDefs.Colors.CYAN.name()),
        () -> assertEquals("\u001B[36m", ZKPolicyDefs.Colors.CYAN.getANSIValue()),

        () -> assertEquals("WHITE", ZKPolicyDefs.Colors.WHITE.name()),
        () -> assertEquals("\u001B[37m", ZKPolicyDefs.Colors.WHITE.getANSIValue()),

        () -> assertEquals("RESET", ZKPolicyDefs.Colors.RESET.name()),
        () -> assertEquals("\u001B[0m", ZKPolicyDefs.Colors.RESET.getANSIValue()));
  }

  @Test
  public void testSchemesEnum() {

    assertAll(() -> assertEquals("WORLD", ZKPolicyDefs.Schemes.WORLD.name()),
        () -> assertEquals("world", ZKPolicyDefs.Schemes.WORLD.getSchemeValue()),

        () -> assertEquals("AUTH", ZKPolicyDefs.Schemes.AUTH.name()),
        () -> assertEquals("auth", ZKPolicyDefs.Schemes.AUTH.getSchemeValue()),

        () -> assertEquals("IP", ZKPolicyDefs.Schemes.IP.name()),
        () -> assertEquals("ip", ZKPolicyDefs.Schemes.IP.getSchemeValue()),

        () -> assertEquals("SASL", ZKPolicyDefs.Schemes.SASL.name()),
        () -> assertEquals("sasl", ZKPolicyDefs.Schemes.SASL.getSchemeValue()),

        () -> assertEquals("DIGEST", ZKPolicyDefs.Schemes.DIGEST.name()),
        () -> assertEquals("digest", ZKPolicyDefs.Schemes.DIGEST.getSchemeValue()));
  }

  @Test
  public void testSchemesIncludesValid() {
    assertAll(() -> assertTrue(ZKPolicyDefs.Schemes.includes("world")),
        () -> assertTrue(ZKPolicyDefs.Schemes.includes("auth")), () -> assertTrue(ZKPolicyDefs.Schemes.includes("ip")),
        () -> assertTrue(ZKPolicyDefs.Schemes.includes("sasl")),
        () -> assertTrue(ZKPolicyDefs.Schemes.includes("digest")));
  }

  @Test
  public void testSchemesIncludesInvalid() {
    assertFalse(ZKPolicyDefs.Schemes.includes("invalidscheme"));
  }

  @Test
  public void testConstructor() {
    assertNotNull(new ZKPolicyDefs());
  }
}