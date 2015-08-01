package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author jl
 */
class PathProjectAnalyzer extends ProjectAnalyzer {
    public void analyzeProject(final IProject project) throws CoreException {
        try {
            IPathVariableManager pathVariableManager = ProjectTransferPlugin.getWorkspace().getPathVariableManager();
            IResource[] resources = project.members();
            for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];
                if (resource.isLinked()) {
                    IPath path = resource.getRawLocation();
                    if (path.getDevice() == null) {
                        // If device is not null, this is definitely _not_ a variable 
                        String variable = path.segment(0);
                        if (!pathVariableManager.isDefined(variable)) {
                            addMissing(variable);
                        }
                    }
                }
            }
            
        } catch (CoreException e) {
            ProjectTransferPlugin.log(e);
        }
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.projecttransfer.ProjectAnalyzer#getMissingFormatString()
     */
    protected String getMissingFormatString() {
        return "PathProjectAnalyzer.missingPathVariable"; //$NON-NLS-1$
    }
}
