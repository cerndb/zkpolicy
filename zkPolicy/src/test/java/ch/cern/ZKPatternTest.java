/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
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
    List<String> globList = new ArrayList<String>();
    globList.add("world:*:*");
    globList.add("sasl:*:c?rw");
    globList.add("gl*b");
    globList.add("gl\\*b");
    globList.add("gl?b");
    globList.add("gl\\?b");
    globList.add("gl[-o]b");
    globList.add("gl\\[-o\\]b");
    globList.add("gl[!a-n!p-z]b");
    globList.add("gl[[!a-n]!p-z]b");
    globList.add("gl[^o]b");
    globList.add("gl?*.()+|^$@%b");
    globList.add("gl[?*.()+|^$@%]b");
    globList.add("gl\\\\b");
    globList.add("\\Qglob\\E");
    globList.add("{glob,regex}");

    globList.add("\\{glob\\}");
    globList.add("{glob\\,regex},");

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
    for (int i = 0; i < globList.size(); i++) {
      assertEquals(expectedList.get(i), patternList.get(i).pattern());
    }
  }

  @Test
  public void testNoArgsConstructor() {
    assertNotNull(new ZKPattern());
  }
}