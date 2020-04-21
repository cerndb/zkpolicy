package ch.cern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ZKPolicyDefs {

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    static enum Colors {
        // This will call enum constructor with one
        // String argument
        BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN("\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"),
        MAGENTA("\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m"), RESET("\u001B[0m");

        // declaring private variable for getting values
        @Getter
        private String ANSIValue;
    }

    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    static enum Schemes {
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

    static interface TerminalConstants {
        static String lastChildIndent = "└─── ";
        static String innerChildIndent = "├─── ";
        static String lastParentIndent = "      ";
        static String innerParentIndent = "│     ";
        static int indentStepLength = lastChildIndent.length();
    }
}