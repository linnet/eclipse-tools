// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/AbstractEditorHandler.java,v 1.1 2004/07/25 13:38:56 linnet Exp $

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

/**
 * This class is used as a base class for editor handlers which can use
 * the class for the editor type to handle.
 * 
 * @author Jesper Kamstrup Linnet
 */
abstract class AbstractEditorHandler implements IEditorHandler {
    public abstract Class getEditorClass();
    
    /* (non-Javadoc)
	 * @see net.sourceforge.eclipsetools.quickmarks.IEditorHandler#isHandler(java.lang.Class)
	 */
	public boolean isHandler(Class clazz) {
        if (getEditorClass() == null) {
            return false;
        }
		return getEditorClass().isAssignableFrom(clazz);
	}

}
