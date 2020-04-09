package ch.cern;

public class ZKPolicyDefs {
    static enum Colors {
        // This will call enum constructor with one
        // String argument
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        MAGENTA("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),
        RESET("\u001B[0m");


        // declaring private variable for getting values
        private String ansiValue;

        // getter method
        public String getANSIValue() {
            return this.ansiValue;
        }

        // enum constructor - cannot be public or protected
        private Colors(String ansiValue)
        {
            this.ansiValue = ansiValue;
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