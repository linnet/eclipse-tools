package dk.kamstruplinnet.projecttransfer;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;


/**
 * @author jl
 */
public class WizardProjectImportPathPage extends AbstractWizardProjectPathPage {
    
    public WizardProjectImportPathPage() {
        super(Messages.getString("WizardProjectImportPathPage.title")); //$NON-NLS-1$
    }

    protected String getContentsLabelText() {
        return Messages.getString("WizardProjectImportPathPage.contents"); //$NON-NLS-1$
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#getPageTitle()
     */
    protected String getPageTitle() {
        return Messages.getString("WizardProjectImportPathPage.projectPath"); //$NON-NLS-1$
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#getPageDescription()
     */
    protected String getPageDescription() {
        return Messages.getString("WizardProjectImportPathPage.rootPath"); //$NON-NLS-1$
    }

    /**
     *
     * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
     */
    public IWizardPage getNextPage() {
        final WizardProjectImportSelectPage selectPage = ((ProjectImportWizard) getWizard()).selectPage;
        final String projectLocation = getProjectLocationFieldValue();

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
                protected void execute(IProgressMonitor monitor) {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                    IProjectDescription[] projectDescriptions = getProjectDescriptions(monitor,
                            projectLocation);
                    selectPage.setProjectDescriptions(projectDescriptions);
                }
            };

        try {
            getContainer().run(false, true, op);
        } catch (InterruptedException e) {
            // Ignore
        } catch (InvocationTargetException e) {
            // ie.- one of the steps resulted in a core exception   
            Throwable t = e.getTargetException();

            MessageDialog.openError(getShell(), Messages.getString("WizardProjectImportPathPage.error_finding_projects"), //$NON-NLS-1$
                Messages.getString("WizardProjectImportPathPage.error") + //$NON-NLS-1$
                t);
        }

        return super.getNextPage();
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#createUserSpecifiedProjectLocationGroup(org.eclipse.swt.widgets.Composite)
     */
    protected void createUserSpecifiedProjectLocationGroup(Composite projectGroup) {
        super.createUserSpecifiedProjectLocationGroup(projectGroup);
        
        Font dialogFont = projectGroup.getFont();

        createExplanationArea(projectGroup, dialogFont, 
                new String[] {
                    "WizardProjectImportPathPage.rootPathExplanation" //$NON-NLS-1$
                });
    }
    
    /**
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    public boolean canFlipToNextPage() {
        return validatePage();
    }

    protected IProject getProject(String descriptionPath) {
        try {
            final IWorkspace workspace = ProjectTransferPlugin.getWorkspace();
            final IProjectDescription projectDescription = workspace.loadProjectDescription(new Path(
                        descriptionPath));

            if (projectDescription != null) {
                String projectName = projectDescription.getName();

                return workspace.getRoot().getProject(projectName);
            }
        } catch (CoreException ce) {
            System.err.println(Messages.getString("WizardProjectImportPathPage.internal_error") + ce); //$NON-NLS-1$
        }

        return null;
    }

    protected IProject getProject(IProjectDescription projectDescription) {
        final IWorkspace workspace = ProjectTransferPlugin.getWorkspace();

        if (projectDescription != null) {
            String projectName = projectDescription.getName();

            return workspace.getRoot().getProject(projectName);
        }

        return null;
    }

    private IProjectDescription[] getProjectDescriptions(
        IProgressMonitor monitor, String projectRootPath) {
        List projectPaths = getAllProjectPaths(monitor, projectRootPath);

        return getProjectDescriptions(monitor, projectPaths);
    }

    private IProjectDescription[] getProjectDescriptions(
        IProgressMonitor monitor, List projectPaths) {
        Collection projectsDescriptions = new ArrayList();

        Collection existingProjects = getExistingProjectDescriptions();
        
        for (Iterator iter = projectPaths.iterator(); iter.hasNext();) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            String descriptionPath = (String) iter.next();
            IProjectDescription projectDescription = getProjectDescription(descriptionPath);

            if (projectDescription != null && !isExistingProject(existingProjects, projectDescription)) {
                projectsDescriptions.add(projectDescription);
            }

            monitor.worked(1);
        }

        return (IProjectDescription[]) projectsDescriptions.toArray(new IProjectDescription[projectsDescriptions.size()]);
    }

    /**
     * @param existingProjects
     * @param projectDescription
     * @return
     */
    private boolean isExistingProject(Collection existingProjects, IProjectDescription projectDescription) {
        for (Iterator iter = existingProjects.iterator(); iter.hasNext();) {
            IProjectDescription existingProject = (IProjectDescription) iter.next();
            if (existingProject.getName().equals(projectDescription.getName())) {
                if (existingProject.getLocation() != null && projectDescription.getLocation() != null)
                    return existingProject.getLocation().equals(projectDescription.getLocation());
                return true;
            }
        }
        return false;
    }

    /**
     * 
     */
    private Collection getExistingProjectDescriptions() {
        IProject[] projects = ProjectTransferPlugin.getWorkspace().getRoot().getProjects();
        Collection result = new HashSet(projects.length);
        for (int i = 0; i < projects.length; i++) {
            IProject project = projects[i];
            try {
                result.add(project.getDescription());
            } catch (CoreException e) {
                // Ignore. If we cannot get the projection description there is no need to do more
            }
        }
        return result;
    }

    protected IProjectDescription getProjectDescription(String descriptionPath) {
        try {
            final IWorkspace workspace = ProjectTransferPlugin.getWorkspace();

            return workspace.loadProjectDescription(new Path(descriptionPath));
        } catch (CoreException ce) {
            System.err.println(Messages.getString("WizardProjectImportPathPage.internal_error") + ce); //$NON-NLS-1$
        }

        return null;
    }

    /**
     * This method recurses through the specified search paths and returns the
     * paths to .project files.
     *
     * @param searchPaths
     * @return String[]
     */
    protected List getAllProjectPaths(IProgressMonitor monitor,
        String searchPath) {
        List result = new ArrayList();
        File rootDir = new File(searchPath);

        FileFilter filter = new FileFilter() {
                /**
                 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
                 */
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }

                    return file.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME);
                }
            };

        recurseAndAddProjectPath(monitor, result, filter, rootDir);

        return result;
    }

    private void recurseAndAddProjectPath(IProgressMonitor monitor,
        List foundPaths, FileFilter filter, File dir) {
        File[] matches = dir.listFiles(filter);

        if (matches != null) {
            for (int i = 0; i < matches.length; i++) {
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                File file = matches[i];

                monitor.worked(1);

                if (file.isDirectory()) {
                    recurseAndAddProjectPath(monitor, foundPaths, filter, file);
                } else {
                    foundPaths.add(file.getAbsolutePath().replace(File.separatorChar,
                            '/'));
                }
            }
        }
    }
}
