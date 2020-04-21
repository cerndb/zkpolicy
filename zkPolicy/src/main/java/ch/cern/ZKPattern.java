package ch.cern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ZKPattern {

    public static List<Pattern> createGlobPatternList(String[] patternStringList) {
        List<Pattern> queryPatternList =  new ArrayList<Pattern>();

        for (String queryGlob : patternStringList) {
            String regexFromGlob = wildcardToRegex(queryGlob);
            Pattern patternRegex = Pattern.compile(regexFromGlob);
            queryPatternList.add(patternRegex);
        }
        return queryPatternList;
    }

    public static List<Pattern> createRegexPatternList(String[] patternStringList) {
        List<Pattern> queryPatternList =  new ArrayList<Pattern>();

        for (String queryGlob : patternStringList) {
            Pattern patternRegex = Pattern.compile(queryGlob);
            queryPatternList.add(patternRegex);
        }
        return queryPatternList;
    }

    private static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch (c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                // escape special regexp-characters
                case '(':
                case ')':
                case '[':
                case ']':
                case '$':
                case '^':
                case '.':
                case '{':
                case '}':
                case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return (s.toString());
    }
}