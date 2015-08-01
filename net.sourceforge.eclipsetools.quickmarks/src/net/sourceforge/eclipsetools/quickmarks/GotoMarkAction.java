// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/GotoMarkAction.java,v 1.2 2004/07/16 01:26:25 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Jesper Kamstrup Linnet - Initial implementation
	Georg Rehfeld - many changes
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.MarkerUtilities;


/**
 * An action to go to an existing Quickmark. This implementation deals with
 * multiple markers with the same number by cycling through them, wether they
 * exist by accident or intention. Cycling can be done forward or backward,
 * wrapping around to the beginning/end when neccessary.
 * 
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class GotoMarkAction extends AbstractMarkAction {

	private static final String GOTO_BOOKMARK_ACTION_PREFIX = 
		QuickmarksPlugin.PLUGIN_ID + ".gotoMark"; //$NON-NLS-1$
	private boolean wrapped = false;

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		int markerNumber = calcMarkerNumber(GOTO_BOOKMARK_ACTION_PREFIX, action.getId());
		if (markerNumber < 0) {
			return;
		}
		if (markerNumber >= 100) {
			// 100-199 are treated as a request to jump to previous marker
			gotoQuickmark(markerNumber - 100, true);
		}
		else {
			gotoQuickmark(markerNumber, false);
		}
	}

	/**
	 * Jumps to the Quickmark with the given number, if there is one.
	 * When neccessary an editor will be opened.
	 * If there are multiple marks with the same number then the previous/next
	 * one from the current selection will be jumped to (when exactly on
	 * a Quickmark that mark isn't considered as a jump target).
	 * If after any Quickmark with that number, the search toggles around
	 * to the first one and vice versa.
	 * 
	 * @param markerNumber  the number of the mark to jump to
	 * @param backward  true, if the previous mark should be jumped to instead
	 *                  of the next one
	 */
	protected void gotoQuickmark(int markerNumber, boolean backward) {
		Integer key = new Integer(markerNumber);
		IResource scope = getScopePreference();
		String scopeName = getPreferenceString(MarkPreferencePage.P_CHOICE_SCOPE);
		scopeName = Messages.getString("MarkPreferencePage.msg.scope." + scopeName); //$NON-NLS-1$
		Map markers = fetchQuickmarks(scope);
		if (! markers.containsKey(key)) {
			showMessage(
				Messages.getFormattedString("GotoMarkAction.msg.NoTargetMark", //$NON-NLS-1$
					key, scopeName),
				true);
			return;
		}
		List markerList = (List) markers.get(key);
		int index = 0;
		String msgSuffix = ""; //$NON-NLS-1$
		if (markerList.size() > 1) {
			index = getMarkerIndex(markerList, backward);
			String msgKey = "GotoMarkAction.msg.suffix.normal"; //$NON-NLS-1$
			if (wrapped) {
				msgKey = "GotoMarkAction.msg.suffix.wrapped"; //$NON-NLS-1$
			}
			msgSuffix = Messages.getFormattedString(
				msgKey, new Integer(index + 1), new Integer(markerList.size()));
		}
		IMarker marker = (IMarker) markerList.get(index);
		IResource resource = marker.getResource();
		if (! (resource instanceof IFile)) {
			showMessage(
				Messages.getFormattedString(
					"GotoMarkAction.msg.MarkNotSetOnFile", key), //$NON-NLS-1$
				true);
			return;
		}
		IWorkbenchPage page = getActivePage();
		if (page == null) {
			showMessage(
				Messages.getFormattedString(
					"GotoMarkAction.msg.NoActivePage", key), //$NON-NLS-1$
				true);
			return;
		}
		// now try to jump and open the right editor, if neccessary
		try {
			page.openEditor(marker, OpenStrategy.activateOnOpen());
			showMessage(
				Messages.getFormattedString(
					"GotoMarkAction.msg.OnQuickmark", //$NON-NLS-1$
					new Object[] { key, msgSuffix, scopeName }),
				false);
		}
		catch (PartInitException e) {
			QuickmarksPlugin.log(e);
			showMessage(
				Messages.getFormattedString(
					"GotoMarkAction.msg.loggedError", key), //$NON-NLS-1$
				true);
		}
	}

	/**
	 * Finds the index of the previous/next marker from the given list.
	 * The search starts from the current text selection and returns the
	 * marker, that is before/after it respecting project, path, name of
	 * the file and charStart, charEnd of the current selection/marker.
	 * 
	 * @param markers  a (sorted) List with IMarkers, must neither be null nor
	 *                 empty
	 * @param previous  indicates, that the index of the previous marker is
	 *                  wanted instead of the next marker
	 * @return  the index of the previous/next marker
	 */
	protected int getMarkerIndex(List markers, boolean previous) {
		if (markers == null || markers.isEmpty()) {
			throw new IllegalArgumentException("Given List is null or empty!"); //$NON-NLS-1$
		}
		wrapped = false;
		int markersSize = markers.size();
		if (markersSize <= 1) {
			return 0;
		}
		ITextSelection selection = getActiveSelection();
		if (selection == null) {
			// no active editor: the first mark is ok
			return 0;
		}

		IFile file = getActiveFile();
		IMarker cmpMarker = null;
		try {
			// create a marker for comparision
			cmpMarker = file.createMarker("org.eclipse.core.resources.marker"); //$NON-NLS-1$
			MarkerUtilities.setLineNumber(cmpMarker, selection.getStartLine() + 1);
			MarkerUtilities.setCharStart(cmpMarker, selection.getOffset());
			MarkerUtilities.setCharEnd(cmpMarker, selection.getOffset() + selection.getLength());
			// compare
			Comparator comparator = new MarkerLocationComparator();
			if (previous) {
				for (int i = markersSize - 1; i >= 0 ; i--) {
					if (comparator.compare(cmpMarker, markers.get(i)) > 0) {
						// text selection is after markers[i]
						return i;
					}
				}
				// text selection is before all markers
				wrapped = true;
				return markersSize - 1;
			}
			else {
				for (int i = 0; i < markersSize; i++) {
					if (comparator.compare(cmpMarker, markers.get(i)) < 0) {
						// text selection is before markers[i]
						return i;
					}
				}
				// text selection is after all markers
				wrapped = true;
				return 0;
			}
		} catch (CoreException e) {
			// log, but ignore this and just return the index to the first marker
			QuickmarksPlugin.log(e);
		}
		finally {
			// get rid of our comparision marker
			if (cmpMarker != null) {
				try {
					cmpMarker.delete();
				}
				catch (CoreException e1) {
					// can't do more than logging the problem
					QuickmarksPlugin.log(e1);
				}
			}
		}
		return 0;
	}

	/**
	 * Displays a message in the currently visible statusline.
	 * 
	 * @param message  the message text to display
	 * @param error    true, if the message is an error
	 */
	protected void showMessage(String message, boolean error) {
		showMessage(message, error, true);
	}
}

// EOF
