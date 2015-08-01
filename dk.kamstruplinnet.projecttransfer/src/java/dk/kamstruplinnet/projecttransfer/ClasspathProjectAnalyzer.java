package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author jl
 */
class ClasspathProjectAnalyzer extends ProjectAnalyzer {
    public void analyzeProject(final IProject project) throws CoreException {
        if (isJavaProject(project)) {
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
            for (int i = 0; i < classpathEntries.length; i++) {
                IClasspathEntry entry = classpathEntries[i];
                if (IClasspathEntry.CPE_VARIABLE == entry.getEntryKind()) {
                    String variable = entry.getPath().segment(0);
                    if (JavaCore.getClasspathVariable(variable) == null) {
                        addMissing(variable);
                    }
                }
            }
        }        
    }

    protected String getMissingFormatString() {
        return "ClasspathProjectAnalyzer.missingClasspathVariable"; //$NON-NLS-1$
    }
}
