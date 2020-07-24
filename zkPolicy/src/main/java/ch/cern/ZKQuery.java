/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.util.List;
import org.apache.zookeeper.data.ACL;

/**
 * Interface for query functions.
 */
public interface ZKQuery {
  String getDescription();
  boolean query(List<ACL> aclList, List<ACL> parentAclList, String path, ZKClient zk, List<String> queryOptions);
}