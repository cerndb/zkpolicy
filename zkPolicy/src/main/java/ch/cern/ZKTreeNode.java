/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import lombok.AccessLevel;

/**
 * Object that represents a ZooKeeper znode.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKTreeNode {

  private String path;
  private List<Byte> data;
  private List<ACL> acl;
  private List<ZKTreeNode> children;
  private Stat stat;

}