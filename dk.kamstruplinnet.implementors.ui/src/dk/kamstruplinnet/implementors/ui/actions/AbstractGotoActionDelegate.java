package dk.kamstruplinnet.implementors.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.ITextEditor;

import dk.kamstruplinnet.implementors.ui.ImplementorsMessages;
import dk.kamstruplinnet.implementors.ui.ImplementorsUI;
import dk.kamstruplinnet.implementors.ui.util.ImplementorsUtility;

/**
 * @see IEditorActionDelegate
 */
public abstract class AbstractGotoActionDelegate implements IEditorActionDelegate {
    private IEditorPart editor;

    /**
     */
    public AbstractGotoActionDelegate() {
        // Nothing here...
    }

    /**
     * @see IEditorActionDelegate#run
     */
    public void run(IAction action) {
        try {
            clearErrorMessage();
            
            if (editor instanceof JavaEditor) {
                try {
                    IJavaElement[] editorElements = SelectionConverter.codeResolve((JavaEditor) editor);
            
                    IJavaElement[] elements = internalSearch(editorElements, getProgressMonitor());
                    if (elements != null) {
                        if (elements.length == 1) {
                        jumpToElement(elements[0]);
                        } else if (elements.length > 1){
                            selectElementToJumpTo(elements);
                        } else {
                            showInfoMessage(getNoResultsMessage());
                        }
                    }
                } catch (JavaModelException e) {
                    ImplementorsUI.log(e);
                }
            }
        } catch (RuntimeException e) {
            ImplementorsUI.log(e);
        }
    }

    protected IJavaElement[] internalSearch(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        if (progressMonitor == null) {
            progressMonitor = new NullProgressMonitor();
        }
        
        progressMonitor.beginTask(getTaskName(), IProgressMonitor.UNKNOWN);
        
        IJavaElement[] result = null;
        try { 
            result = search(elements, progressMonitor);
        } finally {
            progressMonitor.done();
        }
        
        return result;                
    }

    protected abstract IJavaElement[] search(IJavaElement[] elements, IProgressMonitor progressMonitor);

    protected abstract String getTaskName();

    protected abstract String getNoResultsMessage();

    private IProgressMonitor getProgressMonitor() {
        return new NullProgressMonitor();
    }

    /**
     * Select the IJavaElement to jump to from a list of choices.
     * 
     * @param implementors
     */
    private void selectElementToJumpTo(IJavaElement[] implementors) {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(ImplementorsUI.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell(), 
            new AppearanceAwareLabelProvider(AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.ALL_POST_QUALIFIED | JavaElementLabels.APPEND_ROOT_PATH, AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS));

        dialog.setTitle(getSelectMethodTitle());
        dialog.setMessage(getSelectMethodMessage());
        dialog.setElements(implementors);
        dialog.setEmptySelectionMessage(getEmptySelectionMessage());
        dialog.setMultipleSelection(false);
        dialog.setBlockOnOpen(true);
        dialog.setDialogBoundsSettings(ImplementorsUI.getInstance().getDialogSettings(), ElementListSelectionDialog.DIALOG_PERSISTSIZE);
        if (dialog.open() == IDialogConstants.CANCEL_ID)
            return;

        Object[] selectedImplementors = dialog.getResult();
        if (selectedImplementors  != null && selectedImplementors.length == 1) {
            IJavaElement element = (IJavaElement) selectedImplementors [0];
            jumpToElement(element);
        }
    }

    protected abstract String getEmptySelectionMessage();
    protected abstract String getSelectMethodMessage();
    protected abstract String getSelectMethodTitle();

    private void showErrorMessage(String msg) {
        getStatusLineManager().setErrorMessage(msg);
    }

    private void showInfoMessage(String msg) {
        getStatusLineManager().setMessage(msg);
        clearErrorMessage();
    }

    private void clearErrorMessage() {
        showErrorMessage(null);
    }
    
    private IStatusLineManager getStatusLineManager() {
        IStatusLineManager statusLineManager = editor.getEditorSite().getActionBars().getStatusLineManager();
        statusLineManager.setCancelEnabled(true);
        return statusLineManager;
    }

    private void jumpToElement(IJavaElement element) {
        if (element != null) {
            try {
                IEditorPart methodEditor = ImplementorsUtility.openInEditor(element, true);
                if (methodEditor != null) {
                    JavaUI.revealInEditor(methodEditor, element);
                } else {
                    showErrorMessage(ImplementorsMessages.getString("AbstractGotoActionDelegate.error_opening_editor")); //$NON-NLS-1$
                }
            } catch (JavaModelException e) {
                ImplementorsUI.log(e);
            } catch (PartInitException e) {
                ImplementorsUI.log(e);
            }
        }
    }

    /**
     * @see IEditorActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // Do nothing...
    }

    /**
     * @see IEditorActionDelegate#setActiveEditor
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }

    public static IJavaElement getInput(ITextEditor editor) {
        if (editor == null) {
            return null;
        }

        IEditorInput input = editor.getEditorInput();

        IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();

        return manager.getWorkingCopy(input);
    }
}
