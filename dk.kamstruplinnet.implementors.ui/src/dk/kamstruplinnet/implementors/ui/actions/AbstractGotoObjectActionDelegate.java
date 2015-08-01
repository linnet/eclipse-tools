package dk.kamstruplinnet.implementors.ui.actions;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import dk.kamstruplinnet.implementors.ui.ImplementorsUI;


/**
 * Insert the type's description here.
 * @see IEditorActionDelegate
 */
public class AbstractGotoObjectActionDelegate implements IObjectActionDelegate {
    private IMethod mSelectedMethod;

    /**
     * The constructor.
     */
    public AbstractGotoObjectActionDelegate() {
    	// Do nothing...
    }

    /**
     * Insert the method's description here.
     * @see IEditorActionDelegate#run
     */
    public void run(IAction action) {
        try {
            if (mSelectedMethod != null) {
//                TODO
            }
        } catch (Exception e) {
            ImplementorsUI.log(e);
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

    /**
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // Do nothing...
    }
}
