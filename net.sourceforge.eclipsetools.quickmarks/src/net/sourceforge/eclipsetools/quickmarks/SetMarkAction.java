// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/SetMarkAction.java,v 1.2 2004/07/16 01:30:33 deerwood Exp $

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

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.texteditor.MarkerUtilities;


/**
 * An action to set, move or delete a Quickmark. This implementation respects
 * several options set via a preference page.
 * 
 * @see net.sourceforge.eclipsetools.quickmarks.MarkPreferencePage
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class SetMarkAction extends AbstractMarkAction {

	private static final String SET_BOOKMARK_ACTION_PREFIX = 
		QuickmarksPlugin.PLUGIN_ID + ".setMark"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		int markerNumber = calcMarkerNumber(SET_BOOKMARK_ACTION_PREFIX, action.getId());
		if (markerNumber >= 0) {
			setQuickmark(markerNumber);
		}
	}

	/**
	 * Sets or deletes a quickmark with the given number in the active editor.
	 * If there is no such quickmark a new one will be set at the current
	 * location. If a quickmark with the same number exists at the current
	 * location it will be deleted instead. Note, that it is possible to have
	 * more than one quickmark with the same number, by accident or intention.
	 * The method is aware of that. Depending on preferences the handling of
	 * quickmarks with the same number elsewhere is one of:
	 * <ul>
	 *   <li>delete all of them, resulting in a mark move</li>
	 *   <li>delete single ones, but add to multiple ones, resulting in a mark
	 *       move for single ones and a mark addition for multiple ones</li>
	 *   <li>always add new bookmarks</li>
	 * </ul>
	 * The delete condition above works in all cases, the preferences tell, if
	 * the selection must match completely or just start at the same line.
	 * <p>
	 * The marks remember all or part of the current selection depending on
	 * preferences.
	 * <p>
	 * Note also, that the scope for one set of quickmarks can be adjusted to
	 * workspace, project, folder or document in the preferences.
	 * 
	 * @param markerNumber  the number of the mark to set
	 * @see net.sourceforge.eclipsetools.quickmarks.MarkPreferencePage#createFieldEditors()
	 */
	protected void setQuickmark(int markerNumber) {
		Integer key = new Integer(markerNumber);
		//ITextEditor editor = getActiveEditor();
		ITextSelection selection = getActiveSelection();
		if (selection == null) {
			showMessage(
				Messages.getFormattedString("SetMarkAction.msg.NotInEditor", key), //$NON-NLS-1$
				true);
			return;
		}
		IDocument document = getActiveDocument();
		if (document == null) {
			showMessage(
				Messages.getFormattedString("SetMarkAction.msg.NoDocument", key), //$NON-NLS-1$
				true);
			return;
		}
		IFile file = getActiveFile();
		if (file == null) {
			// this often indicates a 'read only' editor (beeing essentially a
			// view), e.g. when opening a class file with associated source or
			// looking at associated source of a binary plugin. Although setting
			// a Quickmark here also would make sense this seems to be impossible.
			showMessage(
				Messages.getFormattedString("SetMarkAction.msg.NoFile", key), //$NON-NLS-1$
				true);
			return;
		}

		int existingCount = 0;
		int deletedCount = 0;

		int charStart = selection.getOffset();
		int charLen = selection.getLength();
		int lineStart = selection.getStartLine() + 1;
		IRegion preferenceSelection = getPreferenceSelection(document, charStart, charLen);
		if (isSelectionNearEOF(document, preferenceSelection)){
			showMessage(
				Messages.getFormattedString("SetMarkAction.msg.NearEOF", key), //$NON-NLS-1$
				true);
			return;
		}

		boolean deletedExplicitely = false;
		IResource scope = getScopePreference();
		String scopeName = getPreferenceString(MarkPreferencePage.P_CHOICE_SCOPE);
		scopeName = Messages.getString("MarkPreferencePage.msg.scope." + scopeName); //$NON-NLS-1$
		Map markers = fetchQuickmarks(scope);
		if (markers.containsKey(key)) {
			List markerList = (List) markers.get(key);
			String deletePreference = getPreferenceString(MarkPreferencePage.P_CHOICE_DELETE);
			String multiplePreference = getPreferenceString(MarkPreferencePage.P_CHOICE_MULTIPLE);
			try {
				existingCount = markerList.size();
				for (int i = 0; i < markerList.size(); i++) {
					// find a marker to delete explicitely
					IMarker marker = (IMarker) markerList.get(i);
					if (MarkPreferencePage.DELETE_MATCH.equals(deletePreference)) {
						Integer markerCharStart =
							(Integer) marker.getAttribute(IMarker.CHAR_START);
						Integer markerCharEnd =
							(Integer) marker.getAttribute(IMarker.CHAR_END);
						if (markerCharStart != null && markerCharEnd != null) {
							// First check, in case the marker was set with a
							// different selection preference than in effect
							// now and the user just has jumped to it, or when
							// the selection preference is 'keepall'.
							if (markerCharStart.intValue() == charStart
								&& markerCharEnd.intValue() == charStart + charLen)
							{
								deselectTasks(marker);
								marker.delete();
								deletedExplicitely = true;
								deletedCount++;
							}
							// Second check, when current selection preference
							// is not 'keepall' and the user has selected
							// something.
							else if (markerCharStart.intValue() == preferenceSelection.getOffset()
								&& markerCharEnd.intValue() == preferenceSelection.getOffset() + preferenceSelection.getLength())
							{
								deselectTasks(marker);
								marker.delete();
								deletedExplicitely = true;
								deletedCount++;
							}
						}
					}
					else {
						// DELETE_LINE.equals(deletePreference)
						Integer markerStartLine =
							(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
						if (markerStartLine != null
							&& markerStartLine.intValue() == lineStart)
						{
							deselectTasks(marker);
							marker.delete();
							deletedExplicitely = true;
							deletedCount++;
						}
					}
				}  // for
				if (deletedExplicitely) {
					String msgSuffix = ""; //$NON-NLS-1$
					if (markerList.size() > 1) {
						msgSuffix = Messages.getFormattedString(
							"SetMarkAction.msg.deleted.suffix", //$NON-NLS-1$
							new Integer(deletedCount), 
							new Integer(markerList.size()));
					}
					showMessage(
						Messages.getFormattedString(
							"SetMarkAction.msg.Deleted", //$NON-NLS-1$
							new Object[] { key, msgSuffix, scopeName }),
						false);
					// we are done
					return;
				}

				// depending on preference delete other marks
				if (MarkPreferencePage.MULTIPLE_ADDASK.equals(multiplePreference)
					&& (markerList.size() > 1))
				{
					MessageDialog dialog = new MessageDialog(
						getShell(),
						Messages.getFormattedString(
							"SetMarkAction.question.title", //$NON-NLS-1$
							key, new Integer(markerList.size())),
						null,
						Messages.getFormattedString(
							"SetMarkAction.question.multiple", //$NON-NLS-1$
							new Object[] {
								key, 
								new Integer(markerList.size()), 
								scopeName }),
						MessageDialog.QUESTION,
						new String[] {
							Messages.getString("SetMarkAction.choice.delete"), //$NON-NLS-1$
							Messages.getString("SetMarkAction.choice.add"), //$NON-NLS-1$
							Messages.getString("SetMarkAction.choice.cancel"), //$NON-NLS-1$
						},
						1
					);
					int result = dialog.open();
					if (result == 0) {
						multiplePreference = MarkPreferencePage.MULTIPLE_ADDNEVER;
					}
					else if (result == 1) {
						multiplePreference = MarkPreferencePage.MULTIPLE_ADDALWAYS;
					}
					else {
						// user requested cancel, either by pressing button
						// "Cancel ..." or pressing ESC: we are done
						return;
					}
				}
				if ((MarkPreferencePage.MULTIPLE_ADDMULTIPLE.equals(multiplePreference)
					|| MarkPreferencePage.MULTIPLE_ADDASK.equals(multiplePreference))
					&& (markerList.size() < 2)
					|| MarkPreferencePage.MULTIPLE_ADDNEVER.equals(multiplePreference))
				{
					for (int i = 0; i < markerList.size(); i++) {
						IMarker marker = (IMarker) markerList.get(i);
						deselectTasks(marker);
						marker.delete();
					}
					deletedCount = markerList.size();
				}
			}
			catch (CoreException e) {
				QuickmarksPlugin.log(e);
				showMessage(
					Messages.getFormattedString(
						"SetMarkAction.msg.loggedDeleteError", key), //$NON-NLS-1$
					true);
				return;
			}
		} // if (markers.containsKey(key))

		// set the location according to preference settings
		Map attributes = new HashMap();
		attributes.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
		MarkerUtilities.setLineNumber(attributes, lineStart);
		MarkerUtilities.setCharStart(attributes, preferenceSelection.getOffset());
		MarkerUtilities.setCharEnd(attributes, preferenceSelection.getOffset() + preferenceSelection.getLength());

		String markerNamePreference = getPreferenceString(MarkPreferencePage.P_STRING_MARKERNAME);
		String selectionText[] = getSelectionText(document, preferenceSelection, 10, 25);
		String markerName = MessageFormat.format(
			markerNamePreference,
			new Object[] {
				key,
				new Integer(lineStart),
				new Integer(preferenceSelection.getOffset()),
				new Integer(preferenceSelection.getLength()),
				file.getName(),
				file.getParent().getName(),
				file.getProjectRelativePath().removeLastSegments(1).toString(),
				file.getProject().getName(),
				new Date(System.currentTimeMillis()),
				selectionText[0],
				selectionText[1],
				selectionText[2],
			});
		MarkerUtilities.setMessage(attributes, markerName);
		attributes.put(QuickmarksPlugin.NUMBER, key);

		try {
			MarkerUtilities.createMarker(
				file,
				attributes,
				QuickmarksPlugin.MARKER_TYPE);
			String msg;
			if (deletedCount == 0) {
				if (existingCount > 0) {
					msg = Messages.getFormattedString(
						"SetMarkAction.msg.Added", //$NON-NLS-1$
						new Object[] { key, new Integer(existingCount), scopeName });
				}
				else {
					msg = Messages.getFormattedString(
						"SetMarkAction.msg.Set", key, scopeName); //$NON-NLS-1$
						
				}
			}
			else if (deletedCount == 1) {
				msg = Messages.getFormattedString(
					"SetMarkAction.msg.Moved", key, scopeName); //$NON-NLS-1$
			}
			else {
				msg = Messages.getFormattedString(
					"SetMarkAction.msg.SetDeleted", //$NON-NLS-1$
					new Object[] { key, new Integer(deletedCount), scopeName });
			}
			showMessage(msg, false);
		}
		catch (CoreException e) {
			QuickmarksPlugin.log(e);
			showMessage(
				Messages.getFormattedString(
					"SetMarkAction.msg.loggedSetError", key), //$NON-NLS-1$
				true);
		}
	}
	
	/**
	 * Determines the strings before, inside and after the given region in the
	 * given document. All consecutive whitespace in the strings is normalized
	 * to just one space. The before and after strings are shortened from
	 * before/behind to the given surroundLen. The region string is
	 * shortened when longer than the given regionLen. Done by retrieving
	 * about half of the allowed length from the start and about another half
	 * from the end of the selection and concatenating both halfs with an
	 * intermediated elipses "...". Note that the returned region string may
	 * be 1 char shorter than allowed (the returned length is always odd).
	 * 
	 * @param document  the document to retrieve the strings from, must not be null
	 * @param region  the region of interest, must not be null
	 * @param surroundLen  the maximum length of the before and after strings
	 * @param regionLen  the maximum length of the region text, should be at least 4
	 * @return  an arry with the 3 strings in natural order (before, region, after),
	 *          never null, but individual entries might be the empty String
	 */
	private String[] getSelectionText(
		IDocument document, IRegion region, int surroundLen, int regionLen)
	{
		String result[] = { "", "", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		int offset = 0;
		int length = 0;
		String string = null;
		regionLen = Math.max(regionLen, 4);
		try {
			// fetch before region string
			offset = Math.max(0, region.getOffset() - 2 * surroundLen);
			length = region.getOffset() - offset;
			string = normalizeWhitespace(document.get(offset, length));
			if (string.length() > surroundLen) {
				string = string.substring(string.length() - surroundLen);
			}
			result[0] = string;

			// fetch region string
			length = region.getLength();
			offset = region.getOffset();
			if (length > 0) {
				string = normalizeWhitespace(document.get(offset, length));
				if (string.length() > regionLen) {
					string = string.substring(0, regionLen / 2 - 2)
						+ "..." //$NON-NLS-1$
						+ string.substring(string.length() - (regionLen / 2 - 2));
				}
				result[1] = string;
			}

			// fetch after region string
			offset = region.getOffset() + region.getLength();
			length = Math.min(document.getLength(), offset + 2 * surroundLen) - offset;
			string = normalizeWhitespace(document.get(offset, length));
			if (string.length() > surroundLen) {
				string = string.substring(0, surroundLen);
			}
			result[2] = string;
		}
		catch (BadLocationException e) {
			// log but ignore
			QuickmarksPlugin.log(e);
		}

		return result;
	}

	/**
	 * Replaces all consecutive occurences of white space with just one space.
	 * 
	 * @param string  the string to normalize
	 * @return  the normalized string
	 */
	private String normalizeWhitespace(String string) {
		StringBuffer buffer = new StringBuffer();
		boolean isFirst = true;
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			switch (c) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
			case '\f':
				if (isFirst) {
					buffer.append(' ');
					isFirst = false;
				}
				break;
			default:
				buffer.append(c);
				isFirst = true;
				break;
			}
		}
		return buffer.toString();
	}

	/**
	 * Gets the modified selection as set in the preferences for this plugin.
	 * The result is delivered as an immutable IRegion value object.
	 * 
	 * @param document  the document for calculating line numbers and offsets
	 * @param charStart  offset of the current selection
	 * @param charLen  length of the current selection
	 * @return  the possibly modified selection
	 */
	protected IRegion getPreferenceSelection(IDocument document, int charStart, int charLen) {
		String selectionPreference = getPreferenceString(MarkPreferencePage.P_CHOICE_SELECTION);
		if (MarkPreferencePage.SELECTION_KEEPSTART.equals(selectionPreference)) {
			return new Region(charStart, 0);
		}
		else if (MarkPreferencePage.SELECTION_LINESTART.equals(selectionPreference)) {
			try {
				IRegion region = document.getLineInformationOfOffset(charStart);
				return new Region(region.getOffset(), 0);
			}
			catch (BadLocationException e) {
				// log but ignore
				QuickmarksPlugin.log(e);
			}
		}
		else if (MarkPreferencePage.SELECTION_WHOLELINE.equals(selectionPreference)) {
			try {
				return document.getLineInformationOfOffset(charStart);
			}
			catch (BadLocationException e) {
				// log but ignore
				QuickmarksPlugin.log(e);
			}
		}
		// default for SELECTION_KEEPALL and in case of BadLocationException
		return new Region(charStart, charLen);
	}

	/**
	 * Tells if the given selection in the given document is at or near end of
	 * file (which makes problems in Eclipse, see
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22487)
	 * <p>
	 * Note, this must be some really nasty bug. If the cursor is at EOL just
	 * before EOF (which SHOULD be valid) all works EXCEPT there is no icon at
	 * all shown in the decoration bars left/right to the editor.
	 * 
	 * @param document  the document to check
	 * @param region  the selection to check
	 * @return  true, if the selection is near EOF, false otherwise
	 */
	protected boolean isSelectionNearEOF(IDocument document, IRegion region) {
		int offset = region.getOffset();
		int docLength = document.getLength();
		if (docLength - offset < 3) {
			return true;
		}
		return false;
	}

	/**
	 * Displays a message in the currently visible statusline.
	 * 
	 * @param message  the message text to display
	 * @param error    true, if the message is an error
	 */
	protected void showMessage(String message, boolean error) {
		showMessage(message, error, false);
	}
}

// EOF
