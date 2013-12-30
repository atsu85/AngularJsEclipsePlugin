package ee.uiboupin.ats.angular.eclipse.hyperlink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;

public abstract class HyperlinkDetectorUtil {

  public static RegionMatch findBestMatch(IDocument document, final int position, Pattern... patterns) {
    Object[] line = getLine(document, position);
    int offset = (Integer) line[1];
    String text = (String) line[0];
    List<Matcher> matches = new ArrayList<Matcher>();
    for (Pattern pattern : patterns) {
      Matcher matcher = pattern.matcher(text);
      if (matcher.find()) {
        matches.add(matcher);
      }
    }
    List<RegionMatch> bestMatches = new ArrayList<RegionMatch>();
    for (Matcher matcher : matches) {
      if (matcher.start() + offset < position && matcher.end() + offset > position) {
        bestMatches.add(new RegionMatch(matcher, offset));
      }
    }
    if (bestMatches.isEmpty()) {
      return null;
    }
    Collections.sort(bestMatches, new Comparator<RegionMatch>() {
      @Override
      public int compare(RegionMatch o1, RegionMatch o2) {
        return (o1.matcher.start(1) - position) - (o2.matcher.start(1) - position);
      }
    });
    return bestMatches.get(0);
  }

  public static Object[] getLine(IDocument document, int offset) {
    String text = document.get();
    if (offset > text.length()) {
      throw new IndexOutOfBoundsException();
    }
    int start = offset, end = offset;
    while (start > 0 && text.charAt(start) != '\n') {
      start--;
    }
    while (end < text.length() && text.charAt(end) != '\n') {
      end++;
    }
    try {
      return new Object[] { text.substring(start > 0 ? start + 1 : 0, end), start > 0 ? start + 1 : 0 };
    } catch (StringIndexOutOfBoundsException e) {
      String msg = "exception from getLine(" + offset + "). start=" + start + "; end=" + end + "; text=" + text;
      throw new RuntimeException(msg, e);
    }
  }

}
