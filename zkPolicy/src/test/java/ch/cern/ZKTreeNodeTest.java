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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;

@TestInstance(Lifecycle.PER_CLASS)
public class ZKTreeNodeTest {
  TestingServer zkTestServer;
  CuratorFramework cli;

  @TempDir
  File testTempDir;

  @Test
  public void testZKTreeNodeConstructor() throws Exception {
    List<ACL> aclList = new ArrayList<ACL>();
    aclList.add(new ACL(30, new Id("world", "anyone")));
    aclList.add(new ACL(30, new Id("digest", "testuser:testpass")));

    List<Byte> child1Data = Arrays.asList(ArrayUtils.toObject("child1 data".getBytes()));
    List<Byte> child2Data = Arrays.asList(ArrayUtils.toObject("child2 data".getBytes()));

    ZKTreeNode child1 = new ZKTreeNode("/path/child1", child1Data, null, null, null);
    ZKTreeNode child2 = new ZKTreeNode("/path/child2", child2Data, null, null, null);
    List<ZKTreeNode> children = new ArrayList<ZKTreeNode>();
    children.add(child1);
    children.add(child2);

    List<Byte> testData = Arrays.asList(ArrayUtils.toObject("test data".getBytes()));
    ZKTreeNode zkTreeNode = new ZKTreeNode("/path", testData, aclList, children, null);
    assertAll(() -> assertEquals("/path", zkTreeNode.getPath()),
        () -> assertArrayEquals(testData.toArray(), zkTreeNode.getData().toArray()),
        () -> assertArrayEquals(children.toArray(), zkTreeNode.getChildren().toArray()),
        () -> assertArrayEquals(aclList.toArray(), zkTreeNode.getAcl().toArray()));
  }
}
