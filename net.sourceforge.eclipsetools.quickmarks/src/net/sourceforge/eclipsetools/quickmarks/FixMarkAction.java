// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/FixMarkAction.java,v 1.1 2004/07/16 00:59:08 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Georg Rehfeld - Initial implementation
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.texteditor.MarkerUtilities;


/**
 * A helper class just for displaying the correct Quickmark icons in the
 * top most editor open shortly after the workbench starts. This classes
 * only public method run() is to be called in an Eclipse UI thread as early
 * as possible.
 * <p>
 * A typical use is from the plugins startup() or better earlyStartup() method
 * (if the Plugin registered the org.eclipse.ui.startup extension point and
 * thus has to implement org.eclipse.ui.IStartup).
 * <p>
 * Note, that both mentioned methods are NOT run in the UI thread! The caller
 * MUST ensure an UI thread by using for instance
 * <pre>
 *     Display.getDefault().asyncExec(new Runnable() {
 *         public void run() {
 *             FixMarkAction fix = new FixMarkAction();
 *             IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
 *             fix.init(window);
 *             fix.run(null); // No real action requiered
 *         }
 *     });
 * </pre>
 * 
 * Impementation note: this class does its work(around) by recreating all
 * markers in the topmost editor silently at a time where this plugin is
 * already activated, thus forcing a QuickmarkImageProvider instance to
 * be asked for the correct image, instead of using the default one.
 * 
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class FixMarkAction extends AbstractMarkAction {

	/**
	 * Triggers the correct display of the Quickmarks icons.
	 * 
	 * @param action  ignored, may be null
	 */
	public void run(IAction action) {
		recreateTopMostMarkers();
	}

	/**
	 * Recreates all Quickmarks in the topmost editor silently.
	 */
	private void recreateTopMostMarkers() {
		// get all Quickmarks from the topmost (active) editor
		IFile file = getActiveFile();
		if (file == null) {
			return;
		}
		Map markers = fetchQuickmarks(file);
		Iterator it1 = markers.keySet().iterator();
		while (it1.hasNext()) {
			Integer key = (Integer) it1.next();
			List markerList = (List) markers.get(key);
			Iterator it2 = markerList.iterator();
			while (it2.hasNext()) {
				IMarker oldMarker = (IMarker) it2.next();
				// set a new identical marker before deleting
				// the old one, as the creation can fail:
				// better to have 2 markers instead of loosing one
				try {
					Map attributes = oldMarker.getAttributes();
					MarkerUtilities.createMarker(
						oldMarker.getResource(),
						attributes,
						oldMarker.getType()
					);
					oldMarker.delete();
				}
				catch (CoreException e) {
					// log but ignore and return, we'll live with default icons
					QuickmarksPlugin.log(e);
					return;
				}
			}
		}
	}

}

// EOF
