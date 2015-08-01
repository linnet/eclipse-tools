package dk.kamstruplinnet.callers.actions;

import dk.kamstruplinnet.callers.CallersPlugin;
import dk.kamstruplinnet.callers.views.CallersView;

import org.eclipse.jdt.core.IMethod;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


/**
 * Insert the type's description here.
 * @see IEditorActionDelegate
 */
public class UpdateCallersObjectActionDelegate implements IObjectActionDelegate {
    private IWorkbenchPart mTargetPart;
    private IMethod mSelectedMethod;

    /**
     * The constructor.
     */
    public UpdateCallersObjectActionDelegate() {
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#run
     */
    public void run(IAction action) {
        try {
            if (mSelectedMethod != null) {
                updateSelectedMethod();
            }
        } catch (Exception e) {
            CallersPlugin.logError("An error occurred in run", e);
        }
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
        mSelectedMethod = null;

        if ((selection != null) && selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection) selection).getFirstElement();

            if (o instanceof IMethod) {
                IMethod method = (IMethod) o;

                mSelectedMethod = method;
            }
        }
    }

    private void updateSelectedMethod() {
        try {
            CallersView callersView = findAndShowCallersView();
            callersView.setMethod(mSelectedMethod);
        } catch (PartInitException e) {
            CallersPlugin.logError("Error switching to callers view", e);
        }
    }

    private CallersView findAndShowCallersView() throws PartInitException {
        IWorkbenchPage workbenchPage = mTargetPart.getSite().getPage();
        CallersView callersView = (CallersView) workbenchPage.showView(CallersView.CALLERS_VIEW_ID);

        return callersView;
    }

    /**
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.mTargetPart = targetPart;
    }
}
