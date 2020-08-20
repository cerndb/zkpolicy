/*
* Copyright © 2020, CERN
* This software is distributed under the terms of the MIT Licence,
* copied verbatim in the file 'LICENSE'. In applying this licence,
* CERN does not waive the privileges and immunities
* granted to it by virtue of its status as an Intergovernmental Organization
* or submit itself to any jurisdiction.
*/
package ch.cern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Constants commonly used throughout the ZooKeeper Policy Audit tool codebase.
 */
public class ZKPolicyDefs {

  enum ExportFormats {
    json, yaml
  }

  enum CheckResults {
    pass, fail, invalidRootPath, notExistingRootPath
  }

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  enum Colors {
    // This will call enum constructor with one
    // String argument
    BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN("\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m"), RESET("\u001B[0m");

    // declaring private variable for getting values
    @Getter
    private String ANSIValue;
  }

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  enum FourLetterWords {
    // This will call enum constructor with one
    // String argument
    CONF("conf"), CONS("cons"), CRST("crst"), DIRS("dirs"), DUMP("dump"), ENVI("envi"), GTMK("gtmk"), RUOK("ruok"),
    STMK("stmk"), SRST("srst"), SRVR("srvr"), STAT("stat"), WCHC("wchc"), WCHP("wchp"), WCHS("wchs"), MNTR("mntr"),
    ISRO("isro"), HASH("hash");

    // declaring private variable for getting values
    @Getter
    private String command;
  }

  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  enum Schemes {
    WORLD("world"), AUTH("auth"), IP("ip"), SASL("sasl"), DIGEST("digest");

    // declaring private variable for getting values
    @Getter
    private String schemeValue;

    public static boolean includes(String value) {
      for (ZKPolicyDefs.Schemes enumElement : values()) {
        if (enumElement.getSchemeValue().equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  static class Cli {
    static class ZkPolicy {
      static final String DESCRIPTION = "ZooKeeper policy auditing tool";
      static final String CONFIG_DESCRIPTION = "YAML configuration file to use (default: ${DEFAULT-VALUE})";
      static final String CONFIG_DEFAULT = "/opt/zkpolicy/conf/config.yml";
      static final String JAAS_FILE_DESCRIPTION = "JAAS file to be used during ZooKeeper authentication";
    }

    static class Audit {
      static final String DESCRIPTION = "Generate full audit report";
      static final String INPUT_DESCRIPTION = "Audit report configuration file (default: ${DEFAULT-VALUE})";
      static final String INPUT_DEFAULT = "/opt/zkpolicy/conf/audit.yml";
      static final String OUTPUT_DESCRIPTION = "Audit report output file";
    }

    static class Check {
      static final String DESCRIPTION = "Check specific znodes for ACL match";
      static final String ROOT_PATH_DESCRIPTION = "Root path to execute query";
      static final String PATH_PATTERN_DESCRIPTION = "Path pattern that must be satisfied to check node";
      static final String ACLS_DESCRIPTION = "ACLs for checking against matching znodes";
      static final String DESCR_DESCRIPTION = "Include check description in output (default: disabled)";
      static final String NEGATE_DESCRIPTION = "Negate result by checking if passed ACLs are not satisfying znode ACL (default: disabled)";
    }

    static class Enforce {
      static final String DESCRIPTION = "Enforce policy on znodes";
      static final String INPUT_DESCRIPTION = "File with policy definitions to enforce";
      static final String INPUT_DEFAULT = "/opt/zkpolicy/conf/enforce.yml";
      static final String POLICY_DESCRIPTION = "Policies to enforce on matching nodes";
      static final String QUERY_NAME_DESCRIPTION = "Query to be executed: ${COMPLETION-CANDIDATES}";
      static final String ROOT_PATH_DESCRIPTION = "Root path to execute query before applying policy";
      static final String ARGS_DESCRIPTION = "Query arguments";
      static final String APPEND_DESCRIPTION = "Append policy ACLs to matching znode's ACL (default: false)";
      static final String DRY_RUN_DESCRIPTION = "Execute enforce in dry-run (show affected nodes without applying changes)";
      static final String ROLLBACK_EXPORT_DESCRIPTION = "File to save pre-enforce state of altered znodes for rollback "
          + "(default: /opt/zkpolicy/rollback/ROLLBACK_STATE_<DATETIME>.yml)";
      static final String SERVICE_POLICY_DESCRIPTION = "Enforce default policy for a specific service. Available services: ${COMPLETION-CANDIDATES}";
      static final String SERVICE_POLICIES_DIR_DESCRIPTION = "Directory for service policies";
      static final String SERVICE_POLICIES_DIR_DEFAULT = "/opt/zkpolicy/conf/policies/";
    }

    static class Export {
      static final String DESCRIPTION = "Export the znode tree";
      static final String TYPE_DESCRIPTION = "Output file format ${COMPLETION-CANDIDATES} (default: json)";
      static final String COMPACT_DESCRIPTION = "Minified export (default: false)";
      static final String OUTPUT_DESCRIPTION = "Output file destination";
      static final String OUTPUT_DEFAULT = "./zkpolicy_export.out";
      static final String ROOT_PATH_DESCRIPTION = "Root path for exported subtree";
    }

    static class Query {
      static final String DESCRIPTION = "Query the znode tree";
      static final String QUERY_NAME_DESCRIPTION = "Query to be executed: ${COMPLETION-CANDIDATES}";
      static final String ROOT_PATH_DESCRIPTION = "Query execution root path";
      static final String ARGS_DESCRIPTION = "Query arguments";
      static final String DESCR_DESCRIPTION = "Include query description in output (default: disabled)";
      static final String COLOR_DESCR_DESCRIPTION = "Include color description in output (default: disabled)";
      static final String LIST_DESCRIPTION = "Return list with query matching znode paths (default: disabled)";
    }

    static class Tree {
      static final String DESCRIPTION = "Visualize znode tree";
      static final String ROOT_PATH_DESCRIPTION = "Root path of requested subtree";
    }

    static class Rollback {
      static final String DESCRIPTION = "Rollback ACLs to pre enforce state";
      static final String INPUT_STATE_DESCRIPTION = "File with ZooKeeper tree state before enforcing";
    }
  }

  static class Queries {
    static final String EXACT_ACL_DESCRIPTION = "Match znodes with ACL equal to the passed ACL argument";
    static final String NO_ACL_DESCRIPTION = "Match znodes with no access control restrictions (ACL equals [world:anyone:cdrwa])";
    static final String SATISFY_ACL_DESCRIPTION = "Match znodes that are accessible by clients with the passed"
        + " authentication info arguments (logical match)";
    static final String NO_SATISFY_ACL_DESCRIPTION = "Match znodes that are not accessible by clients with the passed authentication info arguments";
    static final String PARENT_YES_CHILD_NO_DESCRIPTION = "Match znodes with ACL not equal to their parent's ACL";
    static final String DUPLICATE_ACL_DESCRIPTION = "Match znodes with duplicate ACL entries";
    static final String REGEX_MATCH_ACL_DESCRIPTION = "Match znodes with ACL entries matching the passed regular expression arguments";
    static final String GLOB_MATCH_ACL_DESCRIPTION = "Match znodes with ACL entries matching the passed glob pattern arguments";
    static final String GLOB_MATCH_PATH_DESCRIPTION = "Match znodes with paths matching the passed glob pattern argument";
    static final String REGEX_MATCH_PATH_DESCRIPTION = "Match znodes with paths matching the passed regular expression argument";
  }

  static class Check {
    static final String DESCRIPTION_FORMAT = "Check if znodes under %s with paths matching the %s"
        + " pattern have the following ACL definition set:";
    static final String NEGATE_DESCRIPTION_FORMAT = "Check if znodes under %s with paths matching the %s"
        + " pattern are not accessible by clients with the following authentication credentials:";
  }

  static class Query {
    static final String DESCRIPTION_FORMAT = "Znodes under %s that satisfy the %s query";
  }

  interface TerminalConstants {
    String lastChildIndent = "└─── ";
    String innerChildIndent = "├─── ";
    String lastParentIndent = "      ";
    String innerParentIndent = "│     ";
    int indentStepLength = lastChildIndent.length();

    // Audit report CLI constants
    String sectionSeparator = "\n===============================================\n";
    String subSectionSeparator = "---------------------------------------------------------------------";
  }
}