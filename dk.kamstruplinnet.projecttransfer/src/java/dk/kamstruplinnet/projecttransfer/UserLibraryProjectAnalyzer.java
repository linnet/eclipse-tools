package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.UserLibrary;
import org.eclipse.jdt.internal.core.UserLibraryManager;

/**
 * @author jl
 */
class UserLibraryProjectAnalyzer extends ProjectAnalyzer {
    public void analyzeProject(final IProject project) throws CoreException {
        if (isJavaProject(project)) {
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
            for (int i = 0; i < classpathEntries.length; i++) {
                IClasspathEntry entry = classpathEntries[i];
                if (IClasspathEntry.CPE_CONTAINER == entry.getEntryKind()) {
                    String containerId = entry.getPath().segment(0);
                    String libraryName = entry.getPath().segment(1);
                    if (JavaCore.USER_LIBRARY_CONTAINER_ID.equals(containerId)) {
                        UserLibrary library = UserLibraryManager.getUserLibrary(libraryName);
                        if (library == null) {
                            addMissing(libraryName);
                        }
                    }
                }
            }
        }        
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.projecttransfer.ProjectAnalyzer#getMissingFormatString()
     */
    protected String getMissingFormatString() {
        return "UserLibraryProjectAnalyzer.missingUserLibrary"; //$NON-NLS-1$
    }
}
