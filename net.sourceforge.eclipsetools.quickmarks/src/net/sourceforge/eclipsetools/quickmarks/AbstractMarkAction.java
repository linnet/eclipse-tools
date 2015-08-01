// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/AbstractMarkAction.java,v 1.2 2004/07/25 13:40:10 linnet Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Jesper Kamstrup Linnet - Initial implementation
	Georg Rehfeld - many changes and additions
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.views.tasklist.TaskList;

/**
 * An action to set, delete and move to bookmarks by single keystrokes.
 * The bookmarks are just normal, full blown bookmarks, this action/plugin
 * just enables one to add/remove and goto them quickly: you press some key to
 * set a quick mark and the bookmark is there immediately, named "Quickmark #",
 * where # is a number from 0 to 9 depending on the key pressed (default key
 * binding is Shift+Ctrl+0-9).
 * <p>
 * To jump to a quickmark you press another key and the marked place
 * is immediately visible, opening the document in an appropriate editor, if
 * neccessary (default key binding is Ctrl+0-9).</p>
 * <p>
 * To remove an existing quickmark you move to an existing one and press the
 * "set" key again, this time the quickmark will be removed. Note, that
 * existing quickmarks are deleted automatically when you set them at any 
 * other spot by default. You can set plugin preferences, to change the
 * default behaviour.</p>
 * <p>
 * As there are only 10 quickmark set key bindings available you can configure
 * the plugin to have one set of bookmarks per workspace, per project, per
 * folder or per document.</p>
 * <p>
 * With the current implementation the placement of the marks is tied to the
 * line of the "selection start", <b>not</b> the cursor position, the selection
 * start is always the lower char position of the current selection, regardless
 * of selecting top down or bottom up.</p>
 * <p>
 * Note, that the quick marks also have a menu.</p>
 * <p>
 * This class must be subclassed. Subclasses must implement the
 * <code>run(IAction action)</code> method.</p>
 * 
 * @see IWorkbenchWindowActionDelegate
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
abstract class AbstractMarkAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow activeWindow = null;
	
	/**
	 * A lightweight runnable class taking a message Id in the constructor.
	 * Instances used to clear statusline messages after some time.
	 */
	private class Clearer implements Runnable {
		// remember the message Id to do nothing when this messages was
		// already cleared out.
		private long msgId = -1;

		/**
		 * Constructor taking a message Id. The run() method of this class
		 * only does anything if this Id is still the last one set. Else
		 * the whole thing just does nothing.
		 * 
		 * @param msgId  the message id to clear out eventually
		 */
		public Clearer(long msgId) {
			this.msgId = msgId;
		}

		/**
		 * Clears the last message if it is still the last one set and
		 * really displayed. Else does nothing.
		 */
		public void run() {
			if (msgId == getLastMsgId()) {
				clearMessage();
			}
		}
	} // class Clearer

	/**
	 * A Comparator that compares IMarkers, respecting project, path and name
	 * of the markers resource and the charStart and charEnd attributes.
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with equals:
	 * you can't assume, that <code>(compare(x, y)==0) == (x.equals(y))</code>
	 * is true always, because this Comparator only compares the markers
	 * location and ignores any other IMarker attributes (e.g. the markers type,
	 * message or id). But you <em>can be sure</em> if <code>(x.equals(y))</code>
	 * is true then <code>(compare(x, y)==0)</code> is true too. Thus this
	 * implementation should be usable without problems even in
	 * SortedSet/SortedMap.
	 */
	protected class MarkerLocationComparator implements Comparator, Serializable {

		/* (non-Javadoc)
		 * Compare by project, path, name, charStart, charEnd
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o0, Object o1) {
			if (o0.equals(o1)) {
				return 0;
			}
			// We can safely assume IMarker objects.
			IMarker marker0 = (IMarker) o0;
			IMarker marker1 = (IMarker) o1;
			// Sort by project, path, name, charStart, charEnd, id
			IPath path0 = marker0.getResource().getFullPath();
			IPath path1 = marker1.getResource().getFullPath();
			// project
			String str0 = path0.segment(0);
			String str1 = path1.segment(0);
			int result = str0.compareTo(str1);
			if (result != 0) {
				return result;
			}
			// extract file name for later
			str0 = path0.lastSegment();
			str1 = path1.lastSegment();

			// strip project and file name to leave the path only
			path0 = path0.removeFirstSegments(1);
			path0 = path0.removeLastSegments(1);
			path1 = path1.removeFirstSegments(1);
			path1 = path1.removeLastSegments(1);
			result = path0.toString().compareTo(path1.toString());
			if (result != 0) {
				return result;
			}

			// file name
			result = str0.compareTo(str1);
			if (result != 0) {
				return result;
			}

			long value0 = MarkerUtilities.getCharStart(marker0);
			long value1 = MarkerUtilities.getCharStart(marker1);
			if (value0 < value1) {
				return -1;
			}
			else if (value0 > value1) {
				return 1;
			}
			value0 = MarkerUtilities.getCharEnd(marker0);
			value1 = MarkerUtilities.getCharEnd(marker1);
			if (value0 < value1) {
				return -1;
			}
			else if (value0 > value1) {
				return 1;
			}
			return 0;
		}
	} // class MarkerLocationComparator

	/**
	 * Prevent public instantion of this abstract class.
	 */
	protected AbstractMarkAction() {
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after 
	 * the delegate has been created.
	 * <p>
	 * Currently only messages by us are cleared out. It's a pitty for this
	 * job, that this method is NOT called, when the cursor is moved, but only
	 * when the user actually de/selects something or switches to another
	 * editor/view.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	// TODO: take the opportunity to en/disable goto mark menu actions?
	//       Be aware, this might be expensive, as every used action (potentially
	//       all 20 ones) are messaged with this method!
	public void selectionChanged(IAction action, ISelection selection) {
		clearMessage();
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We cache the window object in order to be able to access several other
	 * needed parts of the UI.
	 * 
	 * @param window  the window to cache
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.activeWindow = window;
	}

	/**
	 * Displays a message in the currently visible statusline.
	 * 
	 * @param message  the message text to display
	 * @param error    true, if the message is an error
	 * @param gotoimg  true, if the message is a goto message, false for set messages
	 */
	protected void showMessage(String message, boolean error, boolean gotoimg) {
		clearMessage();
		IStatusLineManager statusLine = getStatusLine();
		if (statusLine == null) {
			return;
		}
		// get the topmost StatusLineManager to not destroy
		// messages set by views etc.
		while (statusLine instanceof SubStatusLineManager) {
			IContributionManager cb =
				((SubStatusLineManager)statusLine).getParent();
			if (!(cb instanceof IStatusLineManager)) {
				break;
			}
			statusLine = (IStatusLineManager) cb;
		}
		Image img = null;
		int delay = 3;
		if (error) {
			img = QuickmarksPlugin.getDefault().getImage(QuickmarksPlugin.IMG_ERROR);
			statusLine.setMessage(null);
			statusLine.setErrorMessage(img, message);
			getDisplay().beep();
			delay = 5;
		}
		else {
			if (gotoimg) {
				img = QuickmarksPlugin.getDefault().getImage(QuickmarksPlugin.IMG_GOTO);
			}
			else {
				img = QuickmarksPlugin.getDefault().getImage(QuickmarksPlugin.IMG_SET);
			}
			statusLine.setErrorMessage(null);
			statusLine.setMessage(img, message);
		}
		setLastStatusLine(statusLine);
		incrementLastMsgId();
		clearMessageAfter(delay, getLastMsgId());
	}

	/**
	 * Remove any message set by us from the right statusline.
	 */
	protected void clearMessage() {
		IStatusLineManager statusLine = getLastStatusLine();
		if (statusLine == null) {
			return;
		}
		setLastStatusLine(null);
		statusLine.setErrorMessage(null);
		statusLine.setMessage(null);
	}

	/**
	 * Cleares the last message set by us after the given seconds in the given
	 * status line. If that status line is cleared already by other means, e.g.
	 * by a selection change or some other thread this does nothing.
	 * 
	 * @param seconds  the time to elapse before clearing the message
	 * @param statusLine  the status line to clear
	 */
	protected void clearMessageAfter(int seconds, long msgId) {
		getDisplay().timerExec(
			1000 * seconds,
			new Clearer(msgId)
		);
	}

	/**
	 * Retrieves all existing Quickmarks of the given resource recursively.
	 * They are returned as a Map for further processing. The Map's key is
	 * the Quickmark number, the Map's value holds a List with one <b>or more</b>
	 * Quickmarks. Each Set is always sorted ascending by project, path, name
	 * of the file and further by startposition, endposition and finally by
	 * id of the markers contained in the Set.
	 * <p>
	 * There can be more than one Quickmark with the same number for
	 * several reasons: 
	 * <ul>
	 * <li>When the given resource is the Workspace, the projects in it
	 * may be opened and closed. Any marks in closed projects are out of
	 * consideration: they remain to exist, but ain't found or displayed
	 * anywhere as long as the project remains closed. Thus it is possible to
	 * reuse some Quickmarks. When the closed project is opened again, the
	 * previously set Quickmarks show up again and thus there may be more than
	 * one Quickmark with the same number.</li>
	 * <li>When the user changes preferences e.g. from having one set of
	 * quickmarks per document to having one set of quickmarks per workspace
	 * there may be many, many marks with the same number. Instead of deleting
	 * all of them on preference switch time subclasses should be aware of the
	 * issue and try to allow the user navigation and explicit deletion of
	 * no longer important ones.</li>
	 * <li>There is a preference option to work with duplicate markers all the
	 * time.</ul>
	 * <p>
	 * The Quickmarks in the active document are updated to their correct location
	 * (due editing changes).
	 * 
	 * @param resource  the scope resource to find Quickmarks in
	 * @return  a Map with all valid (existing) Quickmarks
	 */
	protected Map fetchQuickmarks(IResource resource) {
		Map result = new HashMap();

		if (resource == null) {
			return result;
		}

		AbstractMarkerAnnotationModel annotationModel = getMarkerAnnotationModel();
		IDocument document = getActiveDocument();
		if (annotationModel != null && document != null) {
			try {
				annotationModel.updateMarkers(document);
			}
			catch (CoreException e) {
				QuickmarksPlugin.log(e);
				// ignored by intention, we work with the markers not updated
			}
		}

		IMarker[] rawMarkers = null;
		try {
			rawMarkers = resource.findMarkers(
				QuickmarksPlugin.MARKER_TYPE, 
				true,
				getFindDepht()
			);
			for (int i = 0; i < rawMarkers.length; i++) {
				IMarker marker = rawMarkers[i];
				if (! marker.exists()) {
					continue;
				}
				int markerNumber = marker.getAttribute(QuickmarksPlugin.NUMBER, -1);
				// If the marker doesn't have the number attribute,
				// there is no point in keeping it.
				if (markerNumber < 0) {
					marker.delete();
					continue;
				}
				Integer key = new Integer(markerNumber);
				List value = null;
				if (result.containsKey(key)) {
					value = (List) result.get(key);
				}
				else {
					value = new ArrayList();
				}
				value.add(marker);
				result.put(key, value);
			}
			// sort the sets
			Iterator it = result.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				List value = (List) result.get(key);
				if (value.size() > 1) {
					Collections.sort(value, new MarkerLocationComparator());
					result.put(key, value);
				}
			}
		}
		catch (CoreException e) {
			// just ignore, we have logged and switched to view the log
			QuickmarksPlugin.log(e);
		}
		return result;
	}

	/**
	 * Returns a meaningful find depth depending on the scope preference. This
	 * implementation returns DEPTH_ZERO for documents, DEPTH_ONE for folders
	 * and DEPTH_INFINITE otherwise.
	 * 
	 * @return  the depth
	 */
	protected int getFindDepht() {
		String scope = getPreferenceString(MarkPreferencePage.P_CHOICE_SCOPE);
		if (MarkPreferencePage.SCOPE_DOCUMENT.equals(scope)) {
			return IResource.DEPTH_ZERO;
		}
		else if (MarkPreferencePage.SCOPE_FOLDER.equals(scope)) {
			return IResource.DEPTH_ONE;
		}
		return IResource.DEPTH_INFINITE;
	}

	/**
	 * Calculates an integer number from the given string 'id' by removing the
	 * given 'prefix' and trying to interpret the rest as an integer.
	 * 
	 * @param prefix  the prefix to remove from the front of the id
	 * @param id      the string to calculate
	 * @return        the calculated integer or -1 if something failes
	 */
	protected int calcMarkerNumber(String prefix, String id) {
		if (id.startsWith(prefix)) {
			try {
				return Integer.parseInt(id.substring(prefix.length()));
			} catch (NumberFormatException nfe) {
				QuickmarksPlugin.log(nfe);
			}
		}
		return -1;
	}

	/**
	 * Returns the active workbench window.
	 * 
	 * @return  the active window
	 */
	protected IWorkbenchWindow getActiveWindow() {
		return activeWindow;
	}

	/**
	 * Returns the shell to use for displaying Dialogs etc.
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		return getActiveWindow().getShell();
	}

	/**
	 * Returns the Display.
	 * 
	 * @return  the Display
	 */
	protected Display getDisplay() {
		return getShell().getDisplay();
	}

	/**
	 * Returns the active workbench page.
	 * 
	 * @return  the active page, or <code>null</code> if none
	 */
	protected IWorkbenchPage getActivePage() {
		return getActiveWindow().getActivePage();
	}

	/**
	 * Fetches the text editor currently in use.
	 * 
	 * @return  the active editor or <code>null</code> if no editor is active
	 */
	protected IEditorPart getActiveEditor() {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			return page.getActiveEditor();
		}
		return null;
	}

	/**
	 * Fetches the editor site of the active editor.
	 * 
	 * @return  the editor site or <code>null</code>
	 */
	protected IEditorSite getEditorSite() {
		IEditorPart editor = getActiveEditor();
		if (editor != null) {
			return editor.getEditorSite();
		}
		return null;
	}

	/**
	 * Fetches the active view.
	 * 
	 * @return  the active view or <code>null</code> if no view is active
	 */
	protected IViewPart getActiveView() {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			IWorkbenchPart part = page.getActivePart();
			if (part instanceof IViewPart) {
				return (IViewPart) part;
			}
		}
		return null;
	}

	/**
	 * Fetches the TaskList if it is amoung the open views,
	 * even if it isn't on top/visible.
	 * 
	 * @return  the TaskList or null
	 */
	protected TaskList getTaskList() {
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			IViewReference refs[] = page.getViewReferences();
			for (int i = 0; i < refs.length; i++) {
				IViewPart part = refs[i].getView(false);
				if (part instanceof TaskList) {
					return (TaskList) part;
				}
			}
		}
		return null;
	}

	/**
	 * Fetches the view site of the active view.
	 * 
	 * @return  the site or <code>null</code>
	 */
	protected IViewSite getViewSite() {
		IViewPart part = getActiveView();
		if (part != null) {
			return part.getViewSite();
		}
		return null;
	}

	/**
	 * Fetches the document provider for the active text editor.
	 * 
	 * @return  the document provider or <code>null</code> if no active editor
	 *          or the active editor is not a text editor
	 */
	protected IDocumentProvider getActiveDocumentProvider() {
		IEditorPart editor = getActiveEditor();
        return QuickmarksPlugin.getDefault().getDocumentProvider(editor);
	}

	/**
	 * Fetches the input under edit.
	 * 
	 * @return  the active input or <code>null</code>, when there is no active
	 *          editor
	 */
	protected IEditorInput getActiveInput() {
		IEditorPart editor = getActiveEditor();
		if (editor == null) {
			return null;
		}
		return editor.getEditorInput();
	}

	/**
	 * Fetches the file represented in the active editor.
	 * 
	 * @return  the file or <code>null</code>, when there is no active file
	 */
	protected IFile getActiveFile() {
		IEditorInput input = getActiveInput();
		if (input != null) {
			return (IFile) input.getAdapter(IFile.class);
		}
		return null;
	}

	/**
	 * Fetches the document just edited.
	 * 
	 * @return  the document or <code>null</code>
	 */
	protected IDocument getActiveDocument() {
		IDocumentProvider provider = getActiveDocumentProvider();
		IEditorInput input = getActiveInput();
		if ((provider != null) && (input != null)) {
			return provider.getDocument(input);
		}
		return null;
	}

	/**
	 * Fetches the marker annotation model for the currently edited document.
	 * Note, that I would prefer to declare this as returning e.g.
	 * IAnnotationModel, but the method to call somewhere else is
	 * <code>updateMarkers(IDocument document)</code> which is not specified
	 * by IAnnotationModel (the only interface inplemented by
	 * AbstractMarkerAnnotationModel).
	 * 
	 * @return  the marker annotation model or <code>null</code>
	 */
	protected AbstractMarkerAnnotationModel getMarkerAnnotationModel() {
		IDocumentProvider provider = getActiveDocumentProvider();
		IDocument document = getActiveDocument();
		if ((provider != null) && (document != null)) {
			IAnnotationModel model = provider.getAnnotationModel(document);
			if (model instanceof AbstractMarkerAnnotationModel) {
				return (AbstractMarkerAnnotationModel) model;
			}
		}
		return null;
	}

	/**
	 * Fetches the text selection from the active text editor.
	 * 
	 * @return  the text selection or <code>null</code>, when there is no
	 *          active editor or it is not a text editor
	 */
	protected ITextSelection getActiveSelection() {
		IEditorPart editor = getActiveEditor();
        return QuickmarksPlugin.getDefault().getTextSelection(editor);
	}

	/**
	 * Fetches the currently visible status line manager. Note, that even if it
	 * looks like Eclipse has just one statusline this is far from beeing the
	 * case. Instead <em>every</em> editor and <em>every</em> view has it's own
	 * status line instance, Eclipse switches visibility when focus changes.
	 * This is important to remember when trying to clear a message, especially
	 * an error message, which supresses display of normal messages: you must
	 * remember the status line you have written the message to, else you might
	 * clear messages in the wrong status line.
	 * <p>
	 * Note also, that the returned status line manager most often is a
	 * SubStatusLineManager and there seem to be views (Taskview for instance)
	 * that write some status to their status line and don't refreh it often,
	 * trusting, that nobody else destroys their message. To solve this use:
	 * <pre>
	 *     IStatusLineManager statusLine = getStatusLine();
	 *     while (statusLine instanceof SubStatusLineManager) {
	 *         IContributionManager cb =
	 *             ((SubStatusLineManager)statusLine).getParent();
	 *         if (!(cb instanceof IStatusLineManager)) {
	 *             break;
	 *         }
	 *         statusLine = (IStatusLineManager) cb;
	 *     }
	 *     statusLine.setMessage(...);
	 * </pre>
	 * 
	 * @return  the currently active status line manager
	 */
	protected IStatusLineManager getStatusLine() {
		IEditorSite esite = getEditorSite();
		if (esite != null) {
			return esite.getActionBars().getStatusLineManager();
		}
		IViewSite vsite = getViewSite();
		if (vsite != null) {
			return vsite.getActionBars().getStatusLineManager();
		}
		return null;
	}

	/**
	 * Sets the status line we have written a message to last.
	 * 
	 * @param statusLine  the status line
	 */
	protected void setLastStatusLine(IStatusLineManager statusLine) {
		// Instance var doesn't work: every action has it's own instance!
		// Instead of using class var we use the plugin singleton for storage.
		QuickmarksPlugin.getDefault().setLastStatusLine(statusLine);
	}

	/**
	 * Fetches the statusline we have written to last.
	 * 
	 * @return  the status line
	 */
	protected IStatusLineManager getLastStatusLine() {
		// Instance var doesn't work: every action has it's own instance!
		// Instead of using class var we use the plugin singleton for storage.
		return QuickmarksPlugin.getDefault().getLastStatusLine();
	}

	/**
	 * Increments the message id.
	 */
	public void incrementLastMsgId() {
		QuickmarksPlugin.getDefault().incrementLastMsgId();
	}

	/**
	 * Fetches the last used message id.
	 * 
	 * @return  the last used message id
	 */
	public long getLastMsgId() {
		return QuickmarksPlugin.getDefault().getLastMsgId();
	}

	/**
	 * Fetches the scope for quickmarks as set in the plugin preferences,
	 * which may be workspace, project, folder or document.
	 * <p>
	 * If no editor is active, the scope for project, folder or document can't
	 * be determined. Instead of bothering the user with an error message then,
	 * this method silently falls back to return the workspace instead. Thus
	 * some (possibly random) document might open and hint the user that way to be
	 * more specific (in case the quickmark number isn't unique amoung the
	 * workspace) or even surprise him / behave as expected when there are no
	 * dups.
	 * 
	 * @return  the IResource scope as set in the preferences or the workspace
	 *          root, when in doubt, never null
	 */
	protected IResource getScopePreference() {
		IResource resource = QuickmarksPlugin.getWorkspace().getRoot();
		IFile file = getActiveFile();
		if (file != null) {
			String scope = getPreferenceString(MarkPreferencePage.P_CHOICE_SCOPE);
			if (MarkPreferencePage.SCOPE_PROJECT.equals(scope)) {
				resource = file.getProject();
			}
			else if (MarkPreferencePage.SCOPE_FOLDER.equals(scope)) {
				resource = file.getParent();
			}
			else if (MarkPreferencePage.SCOPE_DOCUMENT.equals(scope)) {
				resource = file;
			}
		}
		return resource;
	}

	/**
	 * Fetches the string for the given key from the preferences. Convenience
	 * method.
	 * 
	 * @param key  the key to look up
	 * @return  the wanted string or an empty string if there is no such key
	 */
	protected String getPreferenceString(String key) {
		return QuickmarksPlugin.getDefault().getPluginPreferences().getString(key);
	}

	/**
	 * Removes any selection from the TaskList to avoid unwanted moves in our
	 * edited document forced by the TaskList when deleting a marker. The
	 * deselection only occurs, when the TaskList is open (even when not on
	 * top and thus invisible) and it's selection is on the same resource as
	 * the resource in the given marker to avoid unneccessary removal.
	 * 
	 * @param marker  a marker indicating the current resource, may not be null
	 */
	protected void deselectTasks(IMarker marker) {
		TaskList view = getTaskList();
		if (view == null) {
			return;
		}
		ISelection selection = view.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IResource currentResource = marker.getResource();
		Iterator it = ((IStructuredSelection)selection).iterator();
		while (it.hasNext()) {
			// cast is OK, doc and code guarantee IMarker elements
			IResource selectedResource = ((IMarker)it.next()).getResource();
			if (selectedResource.equals(currentResource)) {
				view.setSelection(new StructuredSelection(), false);
				return;
			}
		}
	}
}

// EOF
