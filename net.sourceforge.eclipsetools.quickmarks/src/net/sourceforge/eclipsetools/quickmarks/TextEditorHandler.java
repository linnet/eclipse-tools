// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/TextEditorHandler.java,v 1.1 2004/07/25 13:38:56 linnet Exp $

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

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This is an editor handler for editors which implement ITextEditor.
 * 
 * @author Jesper Kamstrup Linnet
 */
class TextEditorHandler extends AbstractEditorHandler {
    /**
     * 
     */
    public TextEditorHandler() {
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetools.quickmarks.IEditorHandler#getDocumentProvider()
     */
    public IDocumentProvider getDocumentProvider(IEditorPart editor) {
        return getEditor(editor).getDocumentProvider();
    }
    
    /* (non-Javadoc)
     * @see net.sourceforge.eclipsetools.quickmarks.IEditorHandler#getTextSelection()
     */
    public ITextSelection getTextSelection(IEditorPart editor) {
        ISelection selection = getEditor(editor).getSelectionProvider().getSelection();
        if (selection instanceof ITextSelection) {
            return (ITextSelection) selection;
        }
        return null;
    }

    private ITextEditor getEditor(IEditorPart editor) {
        return ((ITextEditor) editor);
    }

	/* (non-Javadoc)
	 * @see net.sourceforge.eclipsetools.quickmarks.AbstractEditorHandler#getEditorClass()
	 */
	public Class getEditorClass() {
		return ITextEditor.class;
	}
}
