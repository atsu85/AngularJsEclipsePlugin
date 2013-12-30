package ee.uiboupin.ats.angular.eclipse.hyperlink;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

/**
 * Matched region from text that could be converted to link
 */
public class RegionMatch {

  public Matcher matcher;
  public int offset;

  public RegionMatch(Matcher matcher, int offset) {
    this.matcher = matcher;
    this.offset = offset;
  }

  public boolean is(Pattern pattern) {
    return matcher.pattern().equals(pattern);
  }

  public interface OpenLinkHandler {
    void openLink(IHyperlink iHyperlink);
  }

  public IHyperlink hyperlink(final String type, int startOffset, int endOffset, final OpenLinkHandler openLinkHandler) {
    final IRegion region = new Region(offset + matcher.start() + startOffset, matcher.end() - matcher.start() - startOffset + endOffset);
    return new IHyperlink() {

      @Override
      public IRegion getHyperlinkRegion() {
        return region;
      }

      @Override
      public String getHyperlinkText() {
        return matcher.group(1);
      }

      @Override
      public String getTypeLabel() {
        return type;
      }

      @Override
      public void open() {
        openLinkHandler.openLink(this);
      }

      @Override
      public String toString() {
        return getTypeLabel() + " --> " + getHyperlinkText();
      }

    };
  }

}