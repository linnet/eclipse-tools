package dk.kamstruplinnet.implementors.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;

import dk.kamstruplinnet.implementors.ui.ImplementorsMessages;
import dk.kamstruplinnet.implementors.ui.ImplementorsUI;

/**
 * @see IEditorActionDelegate
 */
public class GotoImplementorEditorActionDelegate extends AbstractGotoActionDelegate {
    /**
     */
    public GotoImplementorEditorActionDelegate() {
        // Do nothing...
    }

    protected IJavaElement[] search(IJavaElement[] elements, IProgressMonitor progressMonitor) {
        return ImplementorsUI.getInstance().searchForImplementors(elements, progressMonitor);
    }

    protected String getNoResultsMessage() {
        return ImplementorsMessages.getString("GotoImplementorEditorActionDelegate.no_results"); //$NON-NLS-1$
    }

    protected String getEmptySelectionMessage() {
        return ImplementorsMessages.getString("GotoImplementorEditorActionDelegate.empty_selection_message"); //$NON-NLS-1$
    }

    protected String getSelectMethodMessage() {
        return ImplementorsMessages.getString("GotoImplementorEditorActionDelegate.selection_message"); //$NON-NLS-1$
    }

    protected String getSelectMethodTitle() {
        return ImplementorsMessages.getString("GotoImplementorEditorActionDelegate.dialog.title"); //$NON-NLS-1$
    }

    protected String getTaskName() {
        return ImplementorsMessages.getString("GotoImplementorEditorActionDelegate.finder.taskname"); //$NON-NLS-1$
    }
}
