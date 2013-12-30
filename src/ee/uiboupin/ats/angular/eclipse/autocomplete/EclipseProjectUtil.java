package ee.uiboupin.ats.angular.eclipse.autocomplete;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class EclipseProjectUtil {

  public static IProject getProject(ITextViewer textViewer) {
    IPath location = getPath(textViewer);
    String projectName = getProjectName(location);
    IProject project = getProject(projectName);
    if (project == null) {
      throw new RuntimeException("Failed to detect project based on project name '" + projectName + "' extracted from text viewer location " + location);
    }
    return project;
  }

  public static IWorkbenchWindow getActiveWorkbenchWindow() {
    IWorkbench wb = PlatformUI.getWorkbench();
    return wb.getActiveWorkbenchWindow();
  }

  public static IEditorPart gotoFile(IFile file, IWorkbenchWindow window) throws CoreException {
    IWorkbenchPage page = getCurrentPage();
    IMarker marker = file.createMarker(IMarker.TEXT);
    IEditorPart result = IDE.openEditor(page, marker);
    marker.delete();
    return result;
  }

  private static IWorkbenchPage getCurrentPage() {
    IWorkbenchWindow win = getActiveWorkbenchWindow();
    return win.getActivePage();
  }

  private static IPath getPath(ITextViewer textViewer) {
    ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager(); // get the buffer manager
    ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(textViewer.getDocument());
    return textFileBuffer.getLocation();
  }

  private static String getProjectName(IPath location) {
    String locationPS = location.toPortableString();
    if (!locationPS.startsWith("/")) {
      throw new RuntimeException("unexpected location '" + locationPS + "'");
    }
    return locationPS.split("/")[1];
  }

  private static IProject getProject(String projectName) {
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (IProject iProject : projects) {
      if (iProject.getName().equals(projectName)) {
        return iProject;
      }
    }
    return null;
  }

}
