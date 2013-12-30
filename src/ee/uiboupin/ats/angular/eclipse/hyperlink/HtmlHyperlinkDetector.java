package ee.uiboupin.ats.angular.eclipse.hyperlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchWindow;

import ee.uiboupin.ats.angular.eclipse.autocomplete.AngularJsUtil;
import ee.uiboupin.ats.angular.eclipse.autocomplete.EclipseProjectUtil;

public class HtmlHyperlinkDetector extends AbstractHyperlinkDetector {
  public HtmlHyperlinkDetector() {
  }

  @Override
  public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
    IHyperlink link = detectHyperlink(textViewer, region);
    if (link == null) {
      return null;
    }
    return new IHyperlink[] { link };
  }

  private IHyperlink detectHyperlink(ITextViewer textViewer, IRegion region) {
    final IProject project = EclipseProjectUtil.getProject(textViewer);
    final Map<String, String> jsPathByDirectiveName = AngularJsUtil.getJsPathsByAngularDirectiveName(project);
    Map<Pattern, String> pathByPattern = new HashMap<Pattern, String>();
    List<Pattern> patterns = new ArrayList<Pattern>();
    for (Entry<String, String> directiveNameAndPath : jsPathByDirectiveName.entrySet()) {
      String directiveName = directiveNameAndPath.getKey();
      Pattern pattern = Pattern.compile("(" + Pattern.quote(directiveName) + ")");
      patterns.add(pattern);
      String directivePath = directiveNameAndPath.getValue();
      pathByPattern.put(pattern, directivePath);
    }

    RegionMatch match = HyperlinkDetectorUtil.findBestMatch(textViewer.getDocument(), region.getOffset(), patterns.toArray(new Pattern[patterns.size()]));
    if (match != null) {
      for (Entry<Pattern, String> entry : pathByPattern.entrySet()) {
        Pattern pattern = entry.getKey();
        final String directivePath = entry.getValue();
        if (match.is(pattern)) {
          // FIXME refactor
          IHyperlink hyperlink = match.hyperlink(AngularJsUtil.ANGULAR_DIRECTIVE, match.matcher.start(1) - match.matcher.start(), 0, null);
          return new HyperlinkWithFilePath(hyperlink, directivePath) {
            @Override
            public void open() {
              HtmlHyperlinkDetector.this.openLink(directivePath, project);
            }
          };
        }
      }
    }
    return null;
  }

  private void openLink(String filePath, IProject project) {
    IFile file = project.getFile(Path.fromPortableString(filePath));
    if (!file.exists()) {
      throw new RuntimeException("Didn't find file '" + file + "' for directive '" + filePath + "'");
    }
    try {
      IWorkbenchWindow win = EclipseProjectUtil.getActiveWorkbenchWindow();
      EclipseProjectUtil.gotoFile(file, win);
    } catch (CoreException e) {
      throw new RuntimeException("Failed to open file " + file, e);
    }
  }

}
