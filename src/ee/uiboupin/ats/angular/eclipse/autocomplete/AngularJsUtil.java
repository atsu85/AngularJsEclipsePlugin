package ee.uiboupin.ats.angular.eclipse.autocomplete;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public abstract class AngularJsUtil {
  public static String ANGULAR_DIRECTIVE = "angular_directive_controller";

  /** convert given "directive-name" to "directiveName" */
  public static String getDirectiveNameFromHtmlAttribute(String attributeName) {
    StringBuilder sb = new StringBuilder();
    boolean convertNextToUpper = false;
    for (int i = 0; i < attributeName.length(); i++) {
      char ch = attributeName.charAt(i);
      if (ch == '-') {
        convertNextToUpper = true;
        continue;
      } else if (convertNextToUpper) {
        convertNextToUpper = false;
        ch = Character.toUpperCase(ch);
      }
      sb.append(ch);
    }
    return sb.toString();
  }

  /** convert given "directiveName" to "directive-name" */
  private static String getDirectiveNameAsHtmlAttribute(String directiveName) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < directiveName.length(); i++) {
      char ch = directiveName.charAt(i);
      if (Character.isUpperCase(ch)) {
        sb.append('-');
        ch = Character.toLowerCase(ch);
      }
      sb.append(ch);
    }
    return sb.toString();
  }

  /**
   * @return map where value is path to javascript file in the project and key is created based on file name and formatted as reference to angular directive in
   * html
   */
  public static Map<String, String> getJsPathsByAngularDirectiveName(IProject project) {
    // TODO in theory project could have two directives with the same name, but under different modules
    final Map<String, String> jsPathByDirectiveName = new HashMap<String, String>();
    try {
      project.accept(new IResourceVisitor() {
        private static final String SUFFIX_JS = ".js";

        // private static final String SUFFIX_HTML = ".html";

        @Override
        public boolean visit(IResource res) throws CoreException {
          if (res instanceof IFile) {
            IFile file = (IFile) res;
            String name = res.getName();
            if (name.endsWith(SUFFIX_JS)) {
              String directiveName = name.substring(0, name.length() - SUFFIX_JS.length());
              String directiveAttributeName = getDirectiveNameAsHtmlAttribute(directiveName);
              jsPathByDirectiveName.put(directiveAttributeName, file.getProjectRelativePath().toPortableString());
              // } else if (name.endsWith(SUFFIX_HTML)) {
              // TODO
            }
          }
          return true;
        }
      });
    } catch (CoreException e) {
      throw new RuntimeException("Failed to detect JS files", e);
    }
    return jsPathByDirectiveName;
  }
}
