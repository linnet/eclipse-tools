package dk.kamstruplinnet.projecttransfer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;


/**
 * @author jl
 */
public class WizardProjectImportSelectPage extends AbstractWizardProjectSelectPage {
    private IProjectDescription[] allProjectDescriptions;
    private ProjectRoot projectRoot;

    /**
     * Creates a new project reference wizard page.
     *
     * @param pageName the name of this page
     */
    public WizardProjectImportSelectPage() {
        super("wizardProjectSelectPage"); //$NON-NLS-1$
    }

    protected String getPageDescription() {
        return 
        Messages.getString("WizardProjectImportSelectPage.description"); //$NON-NLS-1$
    }

    protected String getPageTitle() {
        return Messages.getString("WizardProjectImportSelectPage.title"); //$NON-NLS-1$
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectSelectPage#getInitialInput()
     */
    protected Object getInitialInput() {
        return getProjectRoot();
    }

    /**
     * Method getProjectRoot.
     * @return Object
     */
    private ProjectRoot getProjectRoot() {
        if (projectRoot == null) {
            projectRoot = new ProjectRoot(allProjectDescriptions);
        }

        return projectRoot;
    }

    /**
     * Returns a content provider for the reference project
     * viewer. It will return all projects in the workspace.
     *
     * @return the content provider
     */
    protected IStructuredContentProvider getContentProvider() {
        return new WorkbenchContentProvider() {
                public Object[] getChildren(Object element) {
                    if (!(element instanceof ProjectRoot)) {
                        return new Object[0];
                    }

                    return ((ProjectRoot) element).getProjects();
                }
            };
    }

    /**
     * Returns the projects selected by the user.
     *
     * @return the selected projects
     */
    public IProjectDescription[] getSelectedProjectDescriptions() {
        Object[] elements = selectedProjectsViewer.getCheckedElements();
        IProjectDescription[] projectDescriptions = new IProjectDescription[elements.length];

        for (int i = 0; i < elements.length; i++) {
            IProject project = (IProject) elements[i];
            projectDescriptions[i] = getProjectRoot().getProjectDescription(project);
        }

        return projectDescriptions;
    }

    /**
     * Method setProjects.
     * @param projects
     */
    public void setProjectDescriptions(
        IProjectDescription[] projectDescriptions) {
        projectRoot = null;
        allProjectDescriptions = projectDescriptions;
        selectedProjectsViewer.setInput(getProjectRoot());
        selectAllProjects(true);
    }
}


class ProjectRoot {
    private IProjectDescription[] mProjectDescriptions;
    private Map mProjects = new HashMap();

    ProjectRoot(IProjectDescription[] projectDescriptions) {
        this.mProjectDescriptions = projectDescriptions;

        if (projectDescriptions != null) {
            for (int i = 0; i < projectDescriptions.length; i++) {
                IProjectDescription description = projectDescriptions[i];
                IProject project = getProject(description);
                mProjects.put(project, projectDescriptions[i]);
            }
        }
    }

    /**
     * Method getProjects.
     * @return IProject[]
     */
    public IProjectDescription[] getProjectDescriptions() {
        return mProjectDescriptions;
    }

    public IProject[] getProjects() {
        if (mProjectDescriptions == null) {
            return new IProject[0];
        }

        IProject[] result = new IProject[mProjectDescriptions.length];

        for (int i = 0; i < mProjectDescriptions.length; i++) {
            result[i] = getProject(mProjectDescriptions[i]);
        }

        return result;
    }

    protected IProject getProject(IProjectDescription projectDescription) {
        final IWorkspace workspace = ProjectTransferPlugin.getWorkspace();

        if (projectDescription != null) {
            String projectName = projectDescription.getName();

            return workspace.getRoot().getProject(projectName);
        }

        return null;
    }

    /**
     * Returns the project description which corresponds to the specified
     * project.
     * @param project
     * @return IProjectDescription
     */
    public IProjectDescription getProjectDescription(IProject project) {
        return (IProjectDescription) mProjects.get(project);
    }
}
