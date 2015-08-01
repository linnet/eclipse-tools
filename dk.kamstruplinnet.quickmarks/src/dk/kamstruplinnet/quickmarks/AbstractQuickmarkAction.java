package dk.kamstruplinnet.quickmarks;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class acts as a super class for the specific quickmark actions (set and goto).
 */
abstract class AbstractQuickmarkAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow activeWindow = null;

    public AbstractQuickmarkAction() {
        // Do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // Do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
        // Do nothing
    }

    /**
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
        this.activeWindow = window;
    }

    /**
     * Builds and returns a Map of IMarker's.
     * @return The markers as a Map. Key = quickmark number (Integer), Value = IMarker instance.   
     */
    protected Map getMarkers() {
        Map result = new HashMap();

        IMarker[] editorMarkers = null;

        try {
            if (getActiveEditor() != null) {
                IResource markerResource = QuickmarksPlugin.getWorkspace().getRoot();
                editorMarkers = markerResource.findMarkers(getMarkerType(), true, IResource.DEPTH_INFINITE);
                for (int j = 0; j < editorMarkers.length; j++) {
                    IMarker marker = editorMarkers[j];
                    int markerNumber = marker.getAttribute(QuickmarksPlugin.NUMBER, -1);
                    
                    // If the marker doesn't have the number attribute, there is no point in keeping it.
                    if (markerNumber < 0) {
                        marker.delete();
                        continue;
                    }

                    Integer key = new Integer(markerNumber);
                    result.put(key, marker);
                }
            }
        } catch (Exception e) {
            QuickmarksPlugin.log(e);
        }

        return result;
    }

    /**
     * Returns the active workbench page.
     */
    protected IWorkbenchPage getActivePage() {
        return getActiveWindow().getActivePage();
    }

    /**
     * Returns the active workbench window.
     */
    protected IWorkbenchWindow getActiveWindow() {
        return activeWindow;
    }

    protected int getMarkerNumber(String prefix, String id) {
        if (id.startsWith(prefix)) {
            try {
                return Integer.parseInt(id.substring(prefix.length()));
            } catch (NumberFormatException nfe) {
                QuickmarksPlugin.debug(nfe);
            }
        }
        return -1;
    }

    protected String getMarkerType() {
        return QuickmarksPlugin.getDefault().getMarkerType();
    }

    /**
     * Returns the IFile represented in the currently selected editor.
     */
    protected IFile getActiveFile() {
        IEditorInput input = getActiveInput();
        if (input != null) {
            return (IFile) input.getAdapter(IFile.class);
        }
        return null;
    }

    private IEditorInput getActiveInput() {
        ITextEditor editor = getActiveEditor();
        if (editor != null) {
            return editor.getEditorInput();
        }
        return null;
    }

    /**
     * @return Returns the activeEditor.
     */
    protected ITextEditor getActiveEditor() {
        IEditorPart editor = getActivePage().getActiveEditor();
        if (editor instanceof ITextEditor) {
            return (ITextEditor) editor;
        }
        return null;
    }
}