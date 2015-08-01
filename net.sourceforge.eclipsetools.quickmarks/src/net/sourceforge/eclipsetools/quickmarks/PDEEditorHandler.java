// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/PDEEditorHandler.java,v 1.2 2004/08/01 04:48:19 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Jesper Kamstrup Linnet - Initial implementation
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;

import java.lang.reflect.Method;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Class implementing the functionality to getting a ITextSelection and a IDocumentProvider for PDE editors.
 * The code uses reflection in order to avoid adding dependencies to the PDE plugins. In addition, this may be
 * reused for implementing other kinds of editor handlers, e.g. for the WSAD editors.
 *   
 * @author Jesper Kamstrup Linnet
 */
class PDEEditorHandler implements IEditorHandler {
    private static final String EDITOR_CLASS = "org.eclipse.pde.internal.ui.editor.PDEMultiPageEditor"; //$NON-NLS-1$
	private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	/**
     * 
     */
    public PDEEditorHandler() {
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetools.quickmarks.IEditorHandler#getDocumentProvider()
     */
    public IDocumentProvider getDocumentProvider(IEditorPart editor) {
        return (IDocumentProvider) invokeGetMethod(editor, "getDocumentProvider"); //$NON-NLS-1$
    }
	
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetools.quickmarks.IEditorHandler#getTextSelection()
     */
    public ITextSelection getTextSelection(IEditorPart editor) {
		ISelection selection = (ISelection) invokeGetMethod(editor, "getSelection"); //$NON-NLS-1$
        if (selection instanceof ITextSelection) {
            return (ITextSelection) selection;
        }
        return null;
    }
    
	private Object invokeGetMethod(IEditorPart editor, String methodName) {
        Object result = null;
		try {
			Method method = editor.getClass().getMethod(methodName, EMPTY_CLASS_ARRAY);
			result = method.invoke(editor, EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			QuickmarksPlugin.log(e);
		}
		return result;
	}

    /* (non-Javadoc)
	 * @see net.sourceforge.eclipsetools.quickmarks.AbstractEditorHandler#isHandler(java.lang.Class)
	 */
	public boolean isHandler(Class clazz) {
        while (!Object.class.equals(clazz)) {
            if (EDITOR_CLASS.equals(clazz.getName())) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
		return false;
	}
}
