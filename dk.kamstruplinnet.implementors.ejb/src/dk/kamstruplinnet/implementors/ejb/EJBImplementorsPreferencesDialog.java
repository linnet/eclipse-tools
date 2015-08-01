package dk.kamstruplinnet.implementors.ejb;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author jl
 */
class EJBImplementorsPreferencesDialog extends Dialog {

    private Text fMask;

    /**
     * @param parentShell
     */
    protected EJBImplementorsPreferencesDialog(Shell parentShell) {
        super(parentShell);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(EJBImplementorsMessages.getString("EJBImplementorsPreferencesDialog.dialog.title")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        initializeDialogUnits(composite);

        GridLayout layout= new GridLayout();
        layout.numColumns= 2;
        composite.setLayout(layout);
        
        GridData gd;
        
        new Label(composite, SWT.NONE).setText(EJBImplementorsMessages.getString("EJBImplementorsPreferencesDialog.mask.label")); //$NON-NLS-1$
        
        fMask= new Text(composite, SWT.BORDER);
        fMask.setText(EJBImplementorsPlugin.getInstance().getDeploymentDescriptorMask());
        gd= new GridData(GridData.GRAB_HORIZONTAL);
        gd.widthHint= convertWidthInCharsToPixels(30);
        fMask.setLayoutData(gd);
        
        Label descriptionLabel= new Label(composite, SWT.NONE);
        descriptionLabel.setText(EJBImplementorsMessages.getString("EJBImplementorsPreferencesDialog.mask-description.label")); //$NON-NLS-1$
        gd= new GridData();
        gd.horizontalSpan= 2;
        descriptionLabel.setLayoutData(gd);
        
        
        return composite;
    }
    
    /**
     * Creates the OK and Cancel buttons.
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(
            parent,
            IDialogConstants.OK_ID,
            IDialogConstants.OK_LABEL,
            true);
        createButton(
            parent,
            IDialogConstants.CANCEL_ID,
            IDialogConstants.CANCEL_LABEL,
            false);
    }

    /* (non-Javadoc)
     * Method declared on Dialog
     */
    protected void okPressed() {
        saveChanges();
        super.okPressed();
    }

    /**
     * 
     */
    private void saveChanges() {
        EJBImplementorsPlugin.getInstance().setDeploymentDescriptorMask(fMask.getText());
    }


}
