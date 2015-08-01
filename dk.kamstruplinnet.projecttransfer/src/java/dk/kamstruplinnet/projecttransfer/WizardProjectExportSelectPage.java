package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.ui.model.WorkbenchContentProvider;

/**
 * @author jl
 */
public class WizardProjectExportSelectPage extends AbstractWizardProjectSelectPage {
    
    /**
     * Creates a new project reference wizard page.
     *
     * @param pageName the name of this page
     */
    public WizardProjectExportSelectPage() {
        super(Messages.getString("WizardProjectExportSelectPage.page")); //$NON-NLS-1$
    }

    protected String getPageDescription() {
        return 
        Messages.getString("WizardProjectExportSelectPage.description"); //$NON-NLS-1$
    }

    protected String getPageTitle() {
        return Messages.getString("WizardProjectExportSelectPage.title"); //$NON-NLS-1$
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
                    if (!(element instanceof IWorkspace)) {
                        return new Object[0];
                    }

                    return ((IWorkspace) element).getRoot().getProjects();
                }
            };
    }
    
    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectSelectPage#getInitialInput()
     */
    protected Object getInitialInput() {
        return ResourcesPlugin.getWorkspace();
    }
    
    /**
     * Returns the referenced projects selected by the user.
     *
     * @return the referenced projects
     */
    public IProject[] getSelectedProjects() {
        Object[] elements = selectedProjectsViewer.getCheckedElements();
        IProject[] projects = new IProject[elements.length];
        System.arraycopy(elements, 0, projects, 0, elements.length);
        return projects;    
    }
    
}
