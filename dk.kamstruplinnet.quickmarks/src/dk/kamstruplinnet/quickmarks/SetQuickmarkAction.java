package dk.kamstruplinnet.quickmarks;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author jl
 */
public class SetQuickmarkAction extends AbstractQuickmarkAction {

    private static final String ADD_BOOKMARK_ACTION_PREFIX = "dk.kamstruplinnet.quickmarks.addQuickmarkAction"; //$NON-NLS-1$

    /**
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action) {
        int markerNumber = getMarkerNumber(ADD_BOOKMARK_ACTION_PREFIX, action.getId());
        if (markerNumber >= 0) {
            addBookmark(markerNumber);
        }
    }

    /**
     * Adds a new quickmark in the active editor. If the same quickmark number
     * already exists (in the workspace), the existing quickmark is replaced
     * with the new (i.e. the quickmark is moved). If the same quickmark already
     * exists in the same location, the quickmark is removed.
     * 
     * @param quickmarkNumber
     */
    private void addBookmark(int quickmarkNumber) {
        ITextEditor editor = getActiveEditor();
        if (editor != null) {
            Integer key = new Integer(quickmarkNumber);

            Map attributes = new HashMap();
            ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();

            int charStart = selection.getOffset();
            int charEnd = charStart;
            MarkerUtilities.setCharStart(attributes, charStart);
            MarkerUtilities.setCharEnd(attributes, charEnd);

            IFile file = getActiveFile();

            String message = MessageFormat.format(Messages.getString("SetQuickmarkAction.quickmarkMessage"), new Object[]{key}); //$NON-NLS-1$
            MarkerUtilities.setMessage(attributes, message);
            attributes.put(QuickmarksPlugin.NUMBER, key);
            attributes.put(QuickmarksPlugin.FILE, file);

            boolean OK = true;
            Map markers = getMarkers();
            if (markers.containsKey(key)) {
                IMarker marker = (IMarker) markers.get(key);
                try {
                    Integer markerCharStart = (Integer) marker.getAttribute(IMarker.CHAR_START);
                    Integer markerCharEnd = (Integer) marker.getAttribute(IMarker.CHAR_END);
                    if (markerCharStart != null && markerCharStart.intValue() == charStart && markerCharEnd != null && markerCharEnd.intValue() == charEnd) {
                        OK = false;
                    }
                    marker.delete();
                } catch (CoreException e) {
                    QuickmarksPlugin.debug(e);
                }
            }

            if (OK) {
                try {
                    MarkerUtilities.createMarker(file, attributes, getMarkerType());
                } catch (Exception e) {
                    QuickmarksPlugin.log(e);
                }
            }
        }
    }
}