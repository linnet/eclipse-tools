package dk.kamstruplinnet.quickmarks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.internal.ide.IMarkerImageProvider;

/**
 * Image provider for the quickmark annotations.
 */
public class QuickmarksImageProvider implements IMarkerImageProvider {
    public QuickmarksImageProvider() {
        super();
    }

    /**
     * Returns the relative path for the image to be used for displaying an
     * marker in the workbench. This path is relative to the plugin location
     * 
     * Returns <code>null</code> if there is no appropriate image.
     * 
     * @param marker
     *            The marker to get an image path for.
     * 
     * @see org.eclipse.jface.resource.FileImageDescriptor
     */
    public String getImagePath(IMarker marker) {
        String iconPath = "icons/";//$NON-NLS-1$
        int markerNumber = getMarkerNumber(marker);
        if (markerNumber >= 0) {
            return iconPath + "quickmark" + markerNumber + ".gif"; //$NON-NLS-1$//$NON-NLS-2$
        }
        return null;
    }

    /**
     * Returns the quickmark number represented by the specified IMarker.
     * 
     * @param marker
     */
    private int getMarkerNumber(IMarker marker) {
        return marker.getAttribute(QuickmarksPlugin.NUMBER, -1);
    }
}