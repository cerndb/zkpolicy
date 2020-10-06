/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;


/**
 * Purge rollback snapshots.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ZKPurgeRollback {
  private static Logger logger = LogManager.getLogger(ZKPurgeRollback.class);

  private File rollbackDir;
  /**
   * Purge rollback snapshots retaining retainCount number of snapshots.
   * 
   * @param retainCount Number of snapshots to retain
   * @throws IOException
   */
  public void purgeRollback(Integer retainCount) throws IOException {
    // Sort files lexicographically
    final File[] files = rollbackDir.listFiles(new RollbackFilenameFilter());

    if (files != null) {
      Arrays.sort(files);
      Integer totalSnapshotNum = files.length;
      Integer cnt = 0;
      for (File rollbackFile : files) {
        if (totalSnapshotNum - cnt > retainCount) {
          logger.info("Purging: " + rollbackFile.getPath());
          if (!rollbackFile.delete()) {
            logger.error("Failed to delete " + rollbackFile.getPath());
            throw new IOException("Failed to delete " + rollbackFile.getPath());
          }
        } else {
          break;
        }
        cnt = cnt + 1;
      }
    }
  }

  static class RollbackFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(final File dir, final String name) {
      return name.matches("^ROLLBACK_STATE.*\\.yml");
    }
  }

}