package dk.kamstruplinnet.quickmarks;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * This class jumps to an existing quickmark.
 */
public class GotoQuickmarkAction extends AbstractQuickmarkAction {

    private static final String GOTO_BOOKMARK_ACTION_PREFIX = "dk.kamstruplinnet.quickmarks.gotoQuickmarkAction"; //$NON-NLS-1$

    /**
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action) {
        int markerNumber = getMarkerNumber(GOTO_BOOKMARK_ACTION_PREFIX, action.getId());
        if (markerNumber >= 0) {
            jumpTo(markerNumber);
        }
    }

    /**
     * This method jumps to the quickmark with the specified number.
     * 
     * @param quickmarkNumber
     */
    private void jumpTo(int quickmarkNumber) {
        Integer key = new Integer(quickmarkNumber);
        Map markers = getMarkers();
        if (markers.containsKey(key)) {
            IMarker marker = (IMarker) markers.get(key);
            try {
                IDE.openEditor(getActivePage(), marker, true);
            } catch (PartInitException e) {
                QuickmarksPlugin.log(e);
            }
        }
    }
}