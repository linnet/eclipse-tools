package dk.kamstruplinnet.projecttransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


/**
 * @author jl
 */
public class ProjectExportWizard extends Wizard implements IImportWizard {
    private WizardProjectExportPathPage pathPage;
    private WizardProjectExportSelectPage selectPage;
    
    public ProjectExportWizard() {
        selectPage = new WizardProjectExportSelectPage();
        pathPage = new WizardProjectExportPathPage();
    }
    
    /**
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        addPage(selectPage);
        addPage(pathPage);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.getString("ProjectExportWizard.title")); //$NON-NLS-1$
        
        Collection selectedProjects = new ArrayList();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element instanceof IProject) {
                selectedProjects.add(element);
            }
        }
        selectPage.selectProjects(selectedProjects);
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        return selectPage.isPageComplete() && pathPage.isPageComplete();
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        IProject[] projects = getSelectedProjects();
        
        IPath path = new Path(pathPage.getProjectLocationFieldValue());
        IPath sourcePath = new Path(pathPage.getSourceRootPathValue());

        ProjectTransferPlugin.getExportHandler().exportProjects(projects, path, sourcePath);

        return true;
    }

    IProject[] getSelectedProjects() {
        return selectPage.getSelectedProjects();
    }
    
    /* Force an update of the path prefix list based on the selected projects.
     * 
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    public IWizardPage getNextPage(IWizardPage page) {
        IWizardPage nextPage = super.getNextPage(page);
        if (nextPage == pathPage) {
            pathPage.updatePathPrefixList();
        }
        return nextPage;
    }
}
