package ch.cern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ZKPattern {

  /**
   * Create a list of Pattern objects from a list of glob expression strings.
   * 
   * @param globStringList Glob expression List
   * @return List of Pattern objects
   */
  public static List<Pattern> createGlobPatternList(List<String> globStringList) {
    List<Pattern> queryPatternList = new ArrayList<Pattern>();

    for (String queryGlob : globStringList) {
      String regexFromGlob = globToRegex(queryGlob);
      Pattern patternRegex = Pattern.compile(regexFromGlob);
      queryPatternList.add(patternRegex);
    }
    return queryPatternList;
  }

  /**
   * Create a list of Pattern objects from a list of regular expression strings.
   * 
   * @param regexStringList
   * @return List of Pattern objects
   */
  public static List<Pattern> createRegexPatternList(List<String> regexStringList) {
    List<Pattern> queryPatternList = new ArrayList<Pattern>();

    for (String queryRegEx : regexStringList) {
      Pattern patternRegex = Pattern.compile(queryRegEx);
      queryPatternList.add(patternRegex);
    }
    return queryPatternList;
  }

  private static String globToRegex(String pattern) {
    StringBuilder sb = new StringBuilder(pattern.length());
    int inGroup = 0;
    int inClass = 0;
    int firstIndexInClass = -1;
    char[] arr = pattern.toCharArray();
    for (int i = 0; i < arr.length; i++) {
      char ch = arr[i];
      switch (ch) {
        case '\\':
          if (++i >= arr.length) {
            sb.append('\\');
          } else {
            char next = arr[i];
            switch (next) {
              case ',':
                // escape not needed
                break;
              case 'Q':
              case 'E':
                // extra escape needed
                sb.append('\\');
                sb.append('\\');
                break;
              default:
                sb.append('\\');
            }
            sb.append(next);
          }
          break;
        case '*':
          if (inClass == 0)
            sb.append(".*");
          else
            sb.append('*');
          break;
        case '?':
          if (inClass == 0)
            sb.append('.');
          else
            sb.append('?');
          break;
        case '[':
          inClass++;
          firstIndexInClass = i + 1;
          sb.append('[');
          break;
        case ']':
          inClass--;
          sb.append(']');
          break;
        case '.':
        case '(':
        case ')':
        case '+':
        case '|':
        case '^':
        case '$':
        case '@':
        case '%':
          if (inClass == 0 || firstIndexInClass == i && ch == '^')
            sb.append('\\');
          sb.append(ch);
          break;
        case '!':
          if (firstIndexInClass == i)
            sb.append('^');
          else
            sb.append('!');
          break;
        case '{':
          inGroup++;
          sb.append('(');
          break;
        case '}':
          inGroup--;
          sb.append(')');
          break;
        case ',':
          if (inGroup > 0)
            sb.append('|');
          else
            sb.append(',');
          break;
        default:
          sb.append(ch);
      }
    }
    return sb.toString();
  }
}