package dk.kamstruplinnet.callers.actions;

import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.views.CallersView;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;


/**
 * Insert the type's description here.
 * @see IEditorActionDelegate
 */
public class UpdateCallersEditorActionDelegate implements IEditorActionDelegate {
    private IEditorPart editor;

    /**
     * The constructor.
     */
    public UpdateCallersEditorActionDelegate() {
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#run
     */
    public void run(IAction action) {
        try {
            updateCurrentMethod();
        } catch (Exception e) {
            CallersPlugin.logDebug("An error occurred: " + e);
        }
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#setActiveEditor
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        this.editor = targetEditor;
    }

    private void updateCurrentMethod() {
        try {
            CallersView callersView = findAndShowCallersView();
            callersView.updateCurrentMethod();
        } catch (PartInitException e) {
            CallersPlugin.logError("Error switching to callers view", e);
        }
    }

    private CallersView findAndShowCallersView() throws PartInitException {
        IWorkbenchPage workbenchPage = editor.getSite().getPage();
        CallersView callersView = (CallersView) workbenchPage.showView(CallersView.CALLERS_VIEW_ID);

        return callersView;
    }

    public String toString() {
        return "UpdateEditorAction";
    }
}
