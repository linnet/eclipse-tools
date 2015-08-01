package dk.kamstruplinnet.projecttransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author jl
 */
public class ProjectTransferHandler implements IProjectImportHandler, IProjectExportHandler {
    private static final String[] PROJECT_FILES = {
            IProjectDescription.DESCRIPTION_FILE_NAME, JavaProject.CLASSPATH_FILENAME
    };

    /**
     * Creates the specified projects.
     * 
     * @param projects
     */
    public void importProjects(final IProjectDescription[] projectDescriptions, Shell shell) {
        final List errorList = new ArrayList();
        final Collection createdProjects = new ArrayList(projectDescriptions.length);
        
        // create the new project operation
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor) {
                monitor.beginTask("", projectDescriptions.length * 2); //$NON-NLS-1$
                for (int i = 0; i < projectDescriptions.length; i++) {
                    IProjectDescription description = projectDescriptions[i];
                    try {
                        IProject project = createProject(description, monitor);
                        if (project != null) {
                            createdProjects.add(project);
                        }
                    } catch (CoreException ce) {
                        ProjectTransferPlugin.log(ce);
                        errorList.add(coreExceptionToString(description, ce));
                    } catch (Exception e) {
                        errorList.add(Messages.getFormattedString("ProjectTransferHandler.error_importing_project", //$NON-NLS-1$ 
                                new String[]{description.getName(), e.getMessage()}));
                    }
                }
            }

        };
        // run the new project creation operation
        try {
            new ProgressMonitorDialog(shell).run(true, true, op);
            if (!errorList.isEmpty()) {
                MessageDialog.openError(shell, Messages.getString("ProjectTransferHandler.error_importing_projects"), //$NON-NLS-1$
                        errorList.toString()); 
            }

            analyzeProjects(createdProjects, shell);
        } catch (InterruptedException e) {
            // Ignore
        } catch (InvocationTargetException e) {
            // ie.- one of the steps resulted in a core exception
            Throwable t = e.getTargetException();
            if (t instanceof CoreException) {
                String errorMessage = coreExceptionToString(null, (CoreException) t);
                ErrorDialog.openError(shell, Messages.getString("ProjectTransferHandler.error_importing_projects"), errorMessage, ((CoreException) t).getStatus()); //$NON-NLS-1$
            }
        }
    }

    /**
     * @param createdProjects
     */
    private void analyzeProjects(Collection createdProjects, Shell shell) {
        IProjectAnalyzer[] projectAnalyzers = ProjectTransferPlugin.getDefault().getProjectAnalyzers();
        
        for (int i = 0; i < projectAnalyzers.length; i++) {
            IProjectAnalyzer analyzer = projectAnalyzers[i];
            analyzer.analyzeProjects(createdProjects);
        }
        presentMissingVariables(shell, projectAnalyzers);
    }

    /**
     * Presents the missing variables and libraries.
     * 
     * TODO: The user should be able to add the variables and libraries from the dialog presenting the missing stuff. 
     * Notes: Add library: JavaCore.newLibraryEntry(...)
     * 
     * @param parent
     * @param missingStuff
     */
    private void presentMissingVariables(Shell parent, IProjectAnalyzer[] projectAnalyzers) {
        StringBuffer variableMsg = new StringBuffer();
        
        for (int i = 0; i < projectAnalyzers.length; i++) {
            IProjectAnalyzer analyzer = projectAnalyzers[i];
            if (analyzer.hasMissing()) {
                variableMsg.append(analyzer.getMissingFormatted());
            }
        }
        
        if (variableMsg.length() > 0) {
            MessageDialog.openInformation(parent, Messages.getString("ProjectTransferHandler.missingVariables.title"), variableMsg.toString()); //$NON-NLS-1$
        }
    }

    /**
     * Returns a string representation of an error message to present to the user.
     * @param description
     * @param ce
     * @return
     */
    String coreExceptionToString(IProjectDescription description, CoreException ce) {
        int errorStatusCode = ce.getStatus().getCode();
        if (errorStatusCode == IResourceStatus.CASE_VARIANT_EXISTS || errorStatusCode == IResourceStatus.PATH_OCCUPIED) {
            return Messages.getFormattedString("ProjectTransferHandler.project_already_exists", //$NON-NLS-1$
                    new String[] { description.getName() }); 
        }
        return Messages.getFormattedString("ProjectTransferHandler.error_importing_project", //$NON-NLS-1$ 
                new String[] { description.getName() });
    }

    protected IProject createProject(final IProjectDescription projectDescription, IProgressMonitor monitor)
            throws CoreException {
        final IProject project = getProject(projectDescription);
        project.create(projectDescription, new SubProgressMonitor(monitor, 1));
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        project.open(new SubProgressMonitor(monitor, 1));
        
        return project;
    }

    protected IProject getProject(IProjectDescription projectDescription) {
        final IWorkspace workspace = ProjectTransferPlugin.getWorkspace();
        if (projectDescription != null) {
            String projectName = projectDescription.getName();
            return workspace.getRoot().getProject(projectName);
        }
        return null;
    }

    public void exportProjects(IProject[] projects, IPath exportPath, IPath sourceRootPath) {
        for (int i = 0; i < projects.length; i++) {
            try {
                IProject project = projects[i];
                copyFiles(project, exportPath, sourceRootPath, PROJECT_FILES);
            } catch (IOException e) {
                System.err.println(Messages.getString("ProjectTransferHandler.error_copying_projects") + e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Method copyFiles.
     * @param project
     * @param path
     * @param filesToCopy
     * 
     *  
     */
    private void copyFiles(IProject project, IPath destinationRootPath,
        IPath sourceRootPath, String[] filesToCopy) throws IOException {
        
        IPath destinationPath = getDestinationPath(project, destinationRootPath, sourceRootPath);
        for (int i = 0; i < filesToCopy.length; i++) {
            File sourceFile = new File(project.getLocation().toFile(), filesToCopy[i]);

            if (sourceFile.exists()) {
                        
                IPath destinationFile = destinationPath.append(filesToCopy[i]);
                copyFile(sourceFile, destinationFile.toFile(), false);
            }
        }
    }

    private IPath getDestinationPath(IProject project, IPath destinationRootPath,
        IPath sourceRootPath) {
        IPath result;

        if (sourceRootPath.isPrefixOf(project.getLocation())) {
            result = destinationRootPath.append(project.getLocation().toString().substring(sourceRootPath.toString().length()));
        } else {
            result = destinationRootPath.append(project.getFullPath());
        }

        return result;
    }

    /**
     * Convienence method to copy a file from a source to a
     * destination specifying if token filtering must be used and if
     * source files may overwrite newer destination files.
     *
     * @throws IOException
     */
    public void copyFile(File sourceFile, File destFile, boolean overwrite)
        throws IOException {
        if (overwrite || !destFile.exists() ||
                    (destFile.lastModified() < sourceFile.lastModified())) {
            if (destFile.exists() && destFile.isFile()) {
                destFile.delete();
            }

            // ensure that parent dir of dest file exists!
            // not using getParentFile method to stay 1.1 compat
            File parent = destFile.getParentFile();

            if (!parent.exists()) {
                parent.mkdirs();
            }

            FileInputStream in = null;
            FileOutputStream out = null;

            try {
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);

                byte[] buffer = new byte[8 * 1024];
                int count = 0;

                do {
                    out.write(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                } while (count != -1);
            } finally {
                if (out != null) {
                    out.close();
                }

                if (in != null) {
                    in.close();
                }
            }
        }
    }
}