package ch.cern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKPatternTest {

  @Test
  public void testCreateGlobPatternList() {
    String[] globList = { "world:*:*", "sasl:*:c?rw", "gl*b", "gl\\*b", "gl?b", "gl\\?b", "gl[-o]b", "gl\\[-o\\]b",
        "gl[!a-n!p-z]b", "gl[[!a-n]!p-z]b", "gl[^o]b", "gl?*.()+|^$@%b", "gl[?*.()+|^$@%]b", "gl\\\\b", "\\Qglob\\E",
        "{glob,regex}", "\\{glob\\}", "{glob\\,regex}," };
    List<Pattern> patternList = ZKPattern.createGlobPatternList(globList);
    List<String> expectedList = new ArrayList<String>();

    expectedList.add("world:.*:.*");
    expectedList.add("sasl:.*:c.rw");
    expectedList.add("gl.*b");
    expectedList.add("gl\\*b");
    expectedList.add("gl.b");
    expectedList.add("gl\\?b");
    expectedList.add("gl[-o]b");
    expectedList.add("gl\\[-o\\]b");
    expectedList.add("gl[^a-n!p-z]b");
    expectedList.add("gl[[^a-n]!p-z]b");
    expectedList.add("gl[\\^o]b");
    expectedList.add("gl..*\\.\\(\\)\\+\\|\\^\\$\\@\\%b");
    expectedList.add("gl[?*.()+|^$@%]b");
    expectedList.add("gl\\\\b");
    expectedList.add("\\\\Qglob\\\\E");
    expectedList.add("(glob|regex)");

    expectedList.add("\\{glob\\}");
    expectedList.add("(glob,regex),");

    assertEquals(expectedList.size(), patternList.size());
    for (int i = 0; i < globList.length; i++) {
      assertEquals(expectedList.get(i), patternList.get(i).pattern());
    }
  }

  @Test
  public void testNoArgsConstructor() {
    assertNotNull(new ZKPattern());
  }
}