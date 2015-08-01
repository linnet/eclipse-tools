// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/IEditorHandler.java,v 1.1 2004/07/25 13:38:56 linnet Exp $

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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * An IEditorHandler implementation is responsible for obtaining an ITextSelection and
 * an IDocumentProvider for the editor it handles. By abstracting this out, it is possible
 * to handle editors other than those which implement ITextEditor. 
 *  
 * @author Jesper Kamstrup Linnet
 */
public interface IEditorHandler {
    
    /**
     * Is this the correct editor handler for the specified class?
     *  
     * @param clazz
     * @return
     */
    public boolean isHandler(Class clazz);
    
    /**
     * Return the document provider for the specified editor 
     * or null if none can be determined.
     * 
     * @param editor
     * @return
     */
    public IDocumentProvider getDocumentProvider(IEditorPart editor);
    
    /**
     * Return the text selection for the specified editor 
     * or null if none can be determined.
     * 
     * @param editor
     * @return
     */
    public ITextSelection getTextSelection(IEditorPart editor);
}
