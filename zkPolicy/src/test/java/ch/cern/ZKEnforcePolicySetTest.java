/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKEnforcePolicySetTest {
  @TempDir
  File testTempDir;

  @Test
  public void testZKEnforcePolicyConstructor() throws Exception {
    File file = new File(testTempDir, "policies_tmp.yml");

    FileWriter fw = new FileWriter(file);
    String policiesTmpContent = "---\n" + "policies:\n" + "  -\n" + "    title: \"Policy 1\"\n" + "    query:\n"
        + "        name: \"globMatchACL\"\n" + "        rootPath: \"/\"\n" + "        args:\n"
        + "          - \"ip:*:*\"\n" + "    append: false\n" + "    acls:\n" + "      - \"ip:127.0.0.3:c\"";

    fw.write(policiesTmpContent);
    fw.flush();
    fw.close();

    ZKEnforcePolicySet zkPolicies = new ZKEnforcePolicySet(file);
    assertAll(() -> assertEquals(1, zkPolicies.getPolicies().size()),
        () -> assertEquals("Policy 1", zkPolicies.getPolicies().get(0).getTitle()));
  }

}