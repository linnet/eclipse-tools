package dk.kamstruplinnet.projecttransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;


/**
 * @author jl
 */
public class WizardProjectExportPathPage extends AbstractWizardProjectPathPage {
    private static String previousSourceRootDirectory = ""; //$NON-NLS-1$

    private Text sourceRootPathField;
    private Button sourceRootPathBrowseButton;
    
    private List prefixList;

    private Button mCreateDirButton;
    
    
    public WizardProjectExportPathPage() {
        super(Messages.getString("WizardProjectExportPathPage.page")); //$NON-NLS-1$
    }

    protected String getContentsLabelText() {
        return Messages.getString("WizardProjectExportPathPage.exportPath"); //$NON-NLS-1$
    }
    
    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#getPageTitle()
     */
    protected String getPageTitle() {
        return Messages.getString("WizardProjectExportPathPage.projectPath"); //$NON-NLS-1$
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#getPageDescription()
     */
    protected String getPageDescription() {
        return Messages.getString("WizardProjectExportPathPage.rootPath"); //$NON-NLS-1$
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#createUserSpecifiedProjectLocationGroup(org.eclipse.swt.widgets.Composite)
     */
    protected void createUserSpecifiedProjectLocationGroup(Composite projectGroup) {
        super.createUserSpecifiedProjectLocationGroup(projectGroup);
        
        Font dialogFont = projectGroup.getFont();

        // Source root
        Label sourceRootLabel = new Label(projectGroup, SWT.NONE);
        sourceRootLabel.setText(Messages.getString("WizardProjectExportPathPage.sourceRootPath")); //$NON-NLS-1$
        sourceRootLabel.setFont(dialogFont);

        // project location entry field
        sourceRootPathField = new Text(projectGroup, SWT.BORDER);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        sourceRootPathField.setLayoutData(data);
        sourceRootPathField.setFont(dialogFont);
        sourceRootPathField.addListener(SWT.Modify, locationModifyListener);

        // browse button
        sourceRootPathBrowseButton = new Button(projectGroup, SWT.PUSH);
        sourceRootPathBrowseButton.setText(Messages.getString("WizardProjectExportPathPage.browse")); //$NON-NLS-1$
        sourceRootPathBrowseButton.setFont(dialogFont);
        sourceRootPathBrowseButton.setLayoutData(getButtonLayoutData());
        sourceRootPathBrowseButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    handleSourcePathBrowseButtonPressed();
                }
            });

        Label prefixSuggestionsLabel = new Label(projectGroup, SWT.WRAP);
        prefixSuggestionsLabel.setText(Messages.getString("WizardProjectExportPathPage.prefixSuggestions")); //$NON-NLS-1$
        prefixSuggestionsLabel.setFont(dialogFont);
        
        GridData prefixGridData = new GridData(GridData.FILL_HORIZONTAL);
        prefixGridData.horizontalSpan = 2;
        prefixGridData.heightHint = 100;
        prefixList = new List(projectGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        prefixList.setLayoutData(prefixGridData);
        prefixList.setFont(dialogFont);
        
        prefixList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                handlePrefixListSelectionChange();
            }
        });
        
        createExplanationArea(projectGroup, dialogFont, 
                new String[] { 
                    "WizardProjectExportPathPage.exportPathExplanation", //$NON-NLS-1$
                    "WizardProjectExportPathPage.sourceRootExplanation"  //$NON-NLS-1$
                }
        );
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#createBrowseButton(org.eclipse.swt.widgets.Composite, org.eclipse.swt.graphics.Font)
     */
    protected void createBrowseButton(Composite projectGroup, Font dialogFont) {
        Composite buttonGroup = new Composite(projectGroup, SWT.NONE);
        GridLayout layout = new GridLayout(2, true);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonGroup.setLayout(layout);
        GridData data = new GridData();
        buttonGroup.setLayoutData(data);
        
        super.createBrowseButton(buttonGroup, dialogFont);
        
        mCreateDirButton = new Button(buttonGroup, SWT.PUSH);
        mCreateDirButton.setText(Messages.getString("WizardProjectExportPathPage.createDir")); //$NON-NLS-1$
        mCreateDirButton.setFont(dialogFont);
        mCreateDirButton.setLayoutData(getButtonLayoutData());
        mCreateDirButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    IPath path = new Path(getProjectLocationFieldValue());
                    if (path.toFile().mkdirs()) {
                        MessageDialog.openInformation(getShell(), Messages.getString("WizardProjectExportPathPage.createDir.success.title"), Messages.getFormattedString("WizardProjectExportPathPage.createDir.success.message", getProjectLocationFieldValue())); //$NON-NLS-1$ //$NON-NLS-2$
                    } else {
                        MessageDialog.openError(getShell(), Messages.getString("WizardProjectExportPathPage.createDir.error.title"), Messages.getFormattedString("WizardProjectExportPathPage.createDir.error.message", getProjectLocationFieldValue())); //$NON-NLS-1$ //$NON-NLS-2$
                    } 
                    updatePageComplete();
                }
            });
    }

    /* (non-Javadoc)
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#updatePageComplete()
     */
    protected void updatePageComplete() {
        updateCreateDirButtonEnablement();
        super.updatePageComplete();
    }
    
    void updateCreateDirButtonEnablement() {
        boolean enabledCreateDirButton = false;
        String projectPath = getProjectLocationFieldValue();
        if (projectPath.length() > 0) {
            IPath path = new Path(projectPath);
            enabledCreateDirButton = path.isValidPath(projectPath) && !path.toFile().exists();
        }
        mCreateDirButton.setEnabled(enabledCreateDirButton);
    }

    protected void handlePrefixListSelectionChange() {
        String selectedPrefix = prefixList.getSelection()[0];
        if (!selectedPrefix.equals(Messages.getString("WizardProjectExportPathPage.noPresetPrefix"))) { //$NON-NLS-1$
            sourceRootPathField.setText(selectedPrefix);
        } else {
            sourceRootPathField.setText(""); //$NON-NLS-1$
        }
    }

    void updatePathPrefixList() {
        prefixList.removeAll();
        prefixList.add(Messages.getString("WizardProjectExportPathPage.noPresetPrefix")); //$NON-NLS-1$
        Collection pathPrefixes = getPathPrefixes(((ProjectExportWizard)getWizard()).getSelectedProjects());
        for (Iterator iter = pathPrefixes.iterator(); iter.hasNext();) {
            String prefix = (String) iter.next();
            prefixList.add(prefix);
        }
    }

    /**
     * @param selectedProjects
     */
    private Collection getPathPrefixes(IProject[] selectedProjects) {
        Collection paths = new ArrayList();
        for (int i = 0; i < selectedProjects.length; i++) {
            IProject selectedProject = selectedProjects[i];
            try {
	            IProjectDescription description = selectedProject.getDescription();
                IPath location = description.getLocation();
                if (location != null) {
                    IPath path = location.makeAbsolute();
                    paths.add(path);
                }
            } catch (CoreException e) {
                // Ignore
            }
        }
        return new PathPrefixComputer(paths).getPrefixes();
    }

    private void handleSourcePathBrowseButtonPressed() {
        String selectedDirectory = browseDirectory(sourceRootPathField, previousSourceRootDirectory);
        if (selectedDirectory != null)
            previousSourceRootDirectory = selectedDirectory;
    }

    /**
     * @see dk.kamstruplinnet.projecttransfer.AbstractWizardProjectPathPage#validatePage()
     */
    protected boolean validatePage() {
        boolean result = super.validatePage();
        if (result) {
            result = validatePath(sourceRootPathField, true);
        }
        return result;
    }

    public String getSourceRootPathValue() {
        return trimPath(sourceRootPathField.getText());
    }
}
