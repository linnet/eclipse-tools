package dk.kamstruplinnet.projecttransfer;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author jl
 */
public class ProjectImportWizard extends Wizard implements IImportWizard {
    private WizardProjectImportPathPage mainPage;
    WizardProjectImportSelectPage selectPage;

    /**
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages();
        mainPage = new WizardProjectImportPathPage();
        selectPage = new WizardProjectImportSelectPage();
        addPage(mainPage);
        addPage(selectPage);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle(Messages.getString("ProjectImportWizard.title")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.IWizard#canFinish()
     */
    public boolean canFinish() {
        return mainPage.isPageComplete() && selectPage.isPageComplete();
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        final IProjectDescription[] projectsToImport = selectPage.getSelectedProjectDescriptions();
        
        ProjectTransferPlugin.getImportHandler().importProjects(projectsToImport, getShell());
        
        return true;
    }
}
