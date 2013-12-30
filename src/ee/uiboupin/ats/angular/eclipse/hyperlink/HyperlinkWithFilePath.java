package ee.uiboupin.ats.angular.eclipse.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class HyperlinkWithFilePath implements IHyperlink {
  private final IHyperlink hyperlink;
  private final String filePath;

  public HyperlinkWithFilePath(IHyperlink hyperlink, String filePath) {
    this.hyperlink = hyperlink;
    this.filePath = filePath;
  }

  @Override
  public IRegion getHyperlinkRegion() {
    return hyperlink.getHyperlinkRegion();
  }

  @Override
  public String getTypeLabel() {
    return hyperlink.getTypeLabel();
  }

  @Override
  public String getHyperlinkText() {
    return hyperlink.getHyperlinkText();
  }

  @Override
  public void open() {
    hyperlink.open();
  }

  public String getFilePath() {
    return filePath;
  }
}