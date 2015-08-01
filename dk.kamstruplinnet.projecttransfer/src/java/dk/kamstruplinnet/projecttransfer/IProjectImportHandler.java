package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.swt.widgets.Shell;

/**
 * @author jl
 */
public interface IProjectImportHandler {

    /**
     * @param projectsToImport
     * @param shell
     */
    void importProjects(IProjectDescription[] projectsToImport, Shell shell);
}
