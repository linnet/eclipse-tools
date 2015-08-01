package dk.kamstruplinnet.implementors.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;

import dk.kamstruplinnet.implementors.ui.ImplementorsMessages;
import dk.kamstruplinnet.implementors.ui.ImplementorsUI;

/**
 * @see IEditorActionDelegate
 */
public class GotoInterfaceEditorActionDelegate extends AbstractGotoActionDelegate {
    /**
     */
    public GotoInterfaceEditorActionDelegate() {
        // Do nothing...
    }

    protected IJavaElement[] search(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        return ImplementorsUI.getInstance().searchForInterfacesAndAbstractTypes(elements, progressMonitor);
    }

    protected String getNoResultsMessage() {
        return ImplementorsMessages.getString("GotoInterfaceEditorActionDelegate.no.results"); //$NON-NLS-1$
    }

    protected String getEmptySelectionMessage() {
        return ImplementorsMessages.getString("GotoInterfaceEditorActionDelegate.empty_selection_message"); //$NON-NLS-1$
    }

    protected String getSelectMethodMessage() {
        return ImplementorsMessages.getString("GotoInterfaceEditorActionDelegate.selection_message"); //$NON-NLS-1$
    }

    protected String getSelectMethodTitle() {
        return ImplementorsMessages.getString("GotoInterfaceEditorActionDelegate.dialog.title"); //$NON-NLS-1$
    }

    protected String getTaskName() {
        return ImplementorsMessages.getString("GotoInterfaceEditorActionDelegate.finder.taskname"); //$NON-NLS-1$
    }
}
