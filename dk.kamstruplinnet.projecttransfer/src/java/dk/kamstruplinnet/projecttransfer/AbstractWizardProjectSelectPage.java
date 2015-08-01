package dk.kamstruplinnet.projecttransfer;

import java.util.Collection;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author jl
 */
abstract class AbstractWizardProjectSelectPage extends WizardPage {
    private static final int PROJECT_LIST_MULTIPLIER = 15;

    // widgets
    CheckboxTableViewer selectedProjectsViewer;
    private Button selectAllButton;
    private Button selectNoneButton;

    private Collection initiallySelectedProjects;

    /**
     * Creates a new project reference wizard page.
     * 
     * @param pageName
     *            the name of this page
     */
    public AbstractWizardProjectSelectPage(String pageName) {
        super(pageName);
        setPageComplete(false);
        setTitle(getPageTitle());
        setDescription(getPageDescription());
    }

    protected abstract String getPageDescription();

    protected abstract String getPageTitle();

    /**
     * (non-Javadoc) Method declared on IDialogPage.
     */
    public void createControl(Composite parent) {
        Font font = parent.getFont();

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setFont(font);

        Label referenceLabel = new Label(composite, SWT.NONE);
        referenceLabel.setText(Messages.getString("AbstractWizardProjectSelectPage.selectedProjects")); //$NON-NLS-1$
        referenceLabel.setFont(font);

        selectedProjectsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        selectedProjectsViewer.getTable().setFont(composite.getFont());

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;

        data.heightHint = getDefaultFontHeight(selectedProjectsViewer.getTable(), PROJECT_LIST_MULTIPLIER);
        selectedProjectsViewer.getTable().setLayoutData(data);
        selectedProjectsViewer.setLabelProvider(getLabelProvider());
        selectedProjectsViewer.setContentProvider(getContentProvider());
        selectedProjectsViewer.setInput(getInitialInput());
        if (initiallySelectedProjects != null) {
            selectProjects(initiallySelectedProjects);
        }

        selectedProjectsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            /**
             * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
             */
            public void selectionChanged(SelectionChangedEvent event) {
                updatePageComplete();
            }
        });
        selectAllButton = new Button(composite, SWT.NONE);
        selectAllButton.setText(Messages.getString("AbstractWizardProjectSelectPage.selectAll")); //$NON-NLS-1$
        selectAllButton.addSelectionListener(new SelectButtonListener(this, true));

        selectNoneButton = new Button(composite, SWT.NONE);
        selectNoneButton.setText(Messages.getString("AbstractWizardProjectSelectPage.deselectAll")); //$NON-NLS-1$
        selectNoneButton.addSelectionListener(new SelectButtonListener(this, false));

        setControl(composite);

        updatePageComplete();
    }

    protected abstract Object getInitialInput();

    /**
     * Method getLabelProvider.
     * 
     * @return IBaseLabelProvider
     */
    protected IBaseLabelProvider getLabelProvider() {
        return new WorkbenchLabelProvider();
    }

    /**
     * Returns a content provider for the reference project viewer. It will
     * return all projects in the workspace.
     * 
     * @return the content provider
     */
    protected abstract IStructuredContentProvider getContentProvider();

    /**
     * Get the defualt widget height for the supplied control.
     * 
     * @return int
     * @param control -
     *            the control being queried about fonts
     * @param lines -
     *            the number of lines to be shown on the table.
     */
    private static int getDefaultFontHeight(Control control, int lines) {
        FontData[] viewerFontData = control.getFont().getFontData();
        int fontHeight = 10;

        //If we have no font data use our guess
        if (viewerFontData.length > 0) {
            fontHeight = viewerFontData[0].getHeight();
        }

        return lines * fontHeight;
    }

    private boolean validatePage() {
        return selectedProjectsViewer.getCheckedElements().length > 0;
    }

    /**
     * Method updatePageComplete.
     */
    public void updatePageComplete() {
        setPageComplete(validatePage());
    }

    /**
     * Method selectAllProjects.
     * 
     * @param selectAll
     */
    public void selectAllProjects(boolean selectAll) {
        selectedProjectsViewer.setAllChecked(selectAll);
        updatePageComplete();
    }

    public void selectProjects(Collection projects) {
        if (selectedProjectsViewer == null) {
            initiallySelectedProjects = projects;
        } else {
            selectedProjectsViewer.setCheckedElements(projects.toArray());
        }
    }
}

class SelectButtonListener implements SelectionListener {
    private AbstractWizardProjectSelectPage selectPage;
    private boolean selectAll;

    public SelectButtonListener(AbstractWizardProjectSelectPage selectPage, boolean selectAll) {
        this.selectPage = selectPage;
        this.selectAll = selectAll;
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        selectProjects();
    }

    /**
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        selectProjects();
    }

    private void selectProjects() {
        selectPage.selectAllProjects(selectAll);
    }
}