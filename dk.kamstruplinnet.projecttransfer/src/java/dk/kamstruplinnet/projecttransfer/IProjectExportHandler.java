package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * @author jl
 */
public interface IProjectExportHandler {

    /**
     * @param projects
     * @param path
     * @param sourcePath
     */
    void exportProjects(IProject[] projects, IPath path, IPath sourcePath);
}
