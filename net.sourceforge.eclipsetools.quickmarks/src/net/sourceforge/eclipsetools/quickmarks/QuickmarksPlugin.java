// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/QuickmarksPlugin.java,v 1.3 2004/08/01 04:48:58 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Jesper Kamstrup Linnet - Initial implementation
	Georg Rehfeld - heavy changes
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;


import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class QuickmarksPlugin
	extends AbstractUIPlugin
	implements IStartup
{
	public static final String NUMBER = "number"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "net.sourceforge.eclipsetools.quickmarks"; //$NON-NLS-1$
	public static final String MARKER_TYPE = PLUGIN_ID + ".quickmark"; //$NON-NLS-1$

	public static final String IMG_ERROR = "img_error"; //$NON-NLS-1$
	public static final String IMG_SET = "img_set"; //$NON-NLS-1$
	public static final String IMG_GOTO = "img_goto"; //$NON-NLS-1$

	//The shared instance.
	private static QuickmarksPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private long lastMsgId = -1;
	private IStatusLineManager lastStatusLine = null;
	
    private Collection editorHandlers = new ArrayList();
    
	/**
	 * Logs an error and openes the log view (if possible).
	 * 
	 * @param t  the throwable to log
	 */
	public static void log(Throwable t) {
		getDefault().getLog().log(
			new Status(
				IStatus.ERROR, 
				PLUGIN_ID, 
				IStatus.ERROR, 
				"Internal Error in Quickmarks plugin: " //$NON-NLS-1$
				+ t.getMessage(), t));
		// switch to log view
		try {
			getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.showView("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
		}
		catch (Exception e) {
			// ignore any exception and live with the log view not
			// automatically shown: the problem is logged at least
			// and an appropriate message shown elsewhere.
		}
	}

	/**
	 * The constructor.
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public QuickmarksPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			// resourceBundle = ResourceBundle.getBundle("net.sourceforge.eclipsetools.quickmarks.plugin"); //$NON-NLS-1$
			resourceBundle = ResourceBundle.getBundle("plugin"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
        
        initEditorHandlers();
	}

	/**
	 * Returns the shared singleton instance.
	 * 
	 * @return  the only instance of this class
	 */
	public static QuickmarksPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace this extension lives in.
	 * 
	 * @return  the workspace
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 * 
	 * @return  the string for the key or the key
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = QuickmarksPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 * 
	 * @return  the resource bundle or <code>null</code>
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Forces display of the correct Quickmark icons as early as possible.
	 * 
	 * Will be called in a separated thread after the workbench initializes,
	 * thus this method delegates it's work to an UI thread by means of
	 * <code>Display.getDefault().asyncExec(new Runnable() { ... }</code>
	 */
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			/**
			 * Recreate all Quickmarks in the topmost editor silently.
			 */
			public void run() {
				FixMarkAction fix = new FixMarkAction();
				IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
				if (window == null) {
					return;
				}
				fix.init(window);
				fix.run(null);
			}
		});
	}

	/** 
	 * Sets default preference values. These values will be used
	 * until some preferences are actually set using Preference dialog.
	 * 
	 * @param store  the store to use
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		// These settings will show up when Preference dialog
		// opens up for the first time.
		store.setDefault(MarkPreferencePage.P_CHOICE_SCOPE, MarkPreferencePage.SCOPE_WORKSPACE);
		store.setDefault(MarkPreferencePage.P_CHOICE_MULTIPLE, MarkPreferencePage.MULTIPLE_ADDMULTIPLE);
		store.setDefault(MarkPreferencePage.P_CHOICE_SELECTION, MarkPreferencePage.SELECTION_KEEPALL);
		store.setDefault(MarkPreferencePage.P_CHOICE_DELETE, MarkPreferencePage.DELETE_MATCH);
		store.setDefault(MarkPreferencePage.P_STRING_MARKERNAME,
			Messages.getString("QuickmarksPlugin.name.Quickmark")); //$NON-NLS-1$
	}

	/**
	 * Fetches the statusline we have written to last.
	 * 
	 * @return  the status line
	 */
	public IStatusLineManager getLastStatusLine() {
		return lastStatusLine;
	}

	/**
	 * Sets the status line we have written a message to last.
	 * 
	 * @param manager  the status line
	 */
	public void setLastStatusLine(IStatusLineManager manager) {
		lastStatusLine = manager;
	}

	/**
	 * Increments the message id.
	 */
	public void incrementLastMsgId() {
		lastMsgId++;
	}

	/**
	 * Fetches the last used message id.
	 * 
	 * @return  the last used message id
	 */
	public long getLastMsgId() {
		return lastMsgId;
	}

	/**
	 * Initializes our image registry with images which are frequently used by
	 * this plugin.
	 * 
	 * @param reg  the ImageRegistry to initialize
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
		//super.initializeImageRegistry(reg);
		IPath path = new Path("icons/quickmark_error.gif"); //$NON-NLS-1$
		URL url = getDescriptor().find(path);
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
		reg.put(IMG_ERROR, descriptor);

		path = new Path("icons/addmark_hover.gif"); //$NON-NLS-1$
		url = getDescriptor().find(path);
		descriptor = ImageDescriptor.createFromURL(url);
		reg.put(IMG_SET, descriptor);

		path = new Path("icons/gotomark_hover.gif"); //$NON-NLS-1$
		url = getDescriptor().find(path);
		descriptor = ImageDescriptor.createFromURL(url);
		reg.put(IMG_GOTO, descriptor);
	}

	/**
	 * Fetches an image to accompany messages for the given type.
	 * 
	 * @param type  the type of image, one of IMG_ERROR, IMG_SET or IMG_GOTO
	 * @return  the image or null
	 */
	public Image getImage(String type) {
		return getImageRegistry().get(type);
	}
    
	/**
	 * Initialize the editor handlers. This is done dynamically in order
	 * to ensure that this will not result in problems in case the classes
	 * do not exist. 
	 */
	private void initEditorHandlers() {
		addEditorHandler(TextEditorHandler.class.getName());
		addEditorHandler("net.sourceforge.eclipsetools.quickmarks.PDEEditorHandler"); //$NON-NLS-1$
	}

	/**
	 * Adds an editor handler of the given type name, when it can be
	 * instantiated dynamically and is of type IEditorHandler.
	 * 
	 * @param className  the name of the handler class
	 */
	private void addEditorHandler(String className) {
		try {
			IEditorHandler handler =
				(IEditorHandler) Class.forName(className).newInstance();
			editorHandlers.add(handler);
		}
		catch (Exception e) {
			// Just ignore since the class itself or related classes cannot be loaded 
		}
	}

	/**
	 * Returns the editor handler for the given editor part.
	 * 
	 * @param editor  the editor part to be handled
	 * @return  the handler or <code>null</code>, when a matching handler does
	 *          not exist
	 */
	public IEditorHandler getEditorHandler(IEditorPart editor) {
		if (editor == null) {
			return null;
		}

		for (Iterator iter = editorHandlers.iterator(); iter.hasNext();) {
			IEditorHandler handler = (IEditorHandler) iter.next();
			if (handler.isHandler(editor.getClass())) {
				return handler;
			}
		}

		return null;
	}

	/**
	 * Returns the text selection in given editor.
	 * 
	 * @param editor  the editor to get the selection from
	 * @return  the text selection or <code>null</code>
	 */
	public ITextSelection getTextSelection(IEditorPart editor) {
		IEditorHandler editorHandler = getEditorHandler(editor);
		if (editorHandler != null) {
			return editorHandler.getTextSelection(editor);
		}
		return null;
	}

	/**
	 * Returns the document provider for the given editor.
	 * 
	 * @param editor  the editor of the document
	 * @return  the document provider or <code>null</code>
	 */
	public IDocumentProvider getDocumentProvider(IEditorPart editor) {
		IEditorHandler editorHandler = getEditorHandler(editor);
		if (editorHandler != null) {
			return editorHandler.getDocumentProvider(editor);
		}
		return null;
	}
}

// EOF
