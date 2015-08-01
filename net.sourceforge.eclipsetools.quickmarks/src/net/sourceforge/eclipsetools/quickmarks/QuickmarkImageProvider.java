// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/QuickmarkImageProvider.java,v 1.1 2004/07/16 00:59:08 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Jesper Kamstrup Linnet - Initial implementation
	Georg Rehfeld - comments, images
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.internal.IMarkerImageProvider;


/**
 * Image provider for the quickmark annotations.
 * 
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class QuickmarkImageProvider implements IMarkerImageProvider {

	/**
	 * Constructs the Image provider.
	 */
	public QuickmarkImageProvider() {
		super();
	}

	/**
	 * Returns the relative path for the image to be used for displaying an
	 * marker in the workbench. This path is relative to the plugin location
	 * 
	 * Returns <code>null</code> if there is no appropriate image.
	 * 
	 * @param marker  the marker instance to get an image path for.
	 * @return  the relative path to an image or null
	 * 
	 * @see org.eclipse.ui.internal.IMarkerImageProvider#getImagePath(org.eclipse.core.resources.IMarker)
	 * @see org.eclipse.jface.resource.FileImageDescriptor
	 */
	public String getImagePath(IMarker marker) {
		int markerNumber = getMarkerNumber(marker);
		if (markerNumber >= 0) {
			return "icons/quickmark" + markerNumber + ".png"; //$NON-NLS-1$//$NON-NLS-2$
		}
		return null;
	}

	/**
	 * Returns the quickmark number recorded in the given IMarker.
	 * 
	 * @param marker  the marker instance to work on
	 * @return  the marker number or -1, when something failes
	 */
	private int getMarkerNumber(IMarker marker) {
		return marker.getAttribute(QuickmarksPlugin.NUMBER, -1);
	}
}

// EOF
