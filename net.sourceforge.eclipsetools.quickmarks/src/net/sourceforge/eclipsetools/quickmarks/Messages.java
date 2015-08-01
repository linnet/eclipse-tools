// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/Messages.java,v 1.1 2004/07/16 00:59:08 deerwood Exp $

/**********************************************************************
Copyright (c) 2004 Jesper Kamstrup Linnet and Georg Rehfeld.
All rights reserved. See http://eclipse-tools.sourceforge.net/quickmarks/.
This program and the accompanying materials are made available under the
terms of the Common Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	Jesper Kamstrup Linnet - Initial implementation
	Georg Rehfeld - comments, new method
**********************************************************************/

package net.sourceforge.eclipsetools.quickmarks;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This static helper class allows easy access to resource strings.
 * It is not intended to be instantiated.
 * 
 * @author Jesper Kamstrup Linnet, eclipse@kamstrup-linnet.dk
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class Messages {

	private static final String BUNDLE_NAME = "net.sourceforge.eclipsetools.quickmarks.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Prevent instantiation.
	 */
	private Messages() {
	}

	/**
	 * Gets a string from the resource bundle and formats it with the argument.
	 *
	 * @param key   the string used to get the bundle value, must not be null
	 * @param arg   the object to be inserted somewhere
	 * @return  the formatted message
	 */
	public static String getFormattedString(String key, Object arg) {
		String format = null;

		try {
			format = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
		}

		if (arg == null) {
			arg = ""; //$NON-NLS-1$
		}

		return MessageFormat.format(format, new Object[] { arg });
	}

	/**
	 * Gets a string from the resource bundle and formats it with both arguments.
	 *
	 * @param key   the string used to get the bundle value, must not be null
	 * @param arg0  the 1st object to be inserted somewhere
	 * @param arg1  the 2nd object to be inserted somewhere
	 * @return  the formatted message
	 */
	public static String getFormattedString(String key, Object arg0, Object arg1) {
		String format = null;

		try {
			format = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-2$ //$NON-NLS-1$
		}

		if (arg0 == null) {
			arg0 = ""; //$NON-NLS-1$
		}
		if (arg1 == null) {
			arg1 = ""; //$NON-NLS-1$
		}

		return MessageFormat.format(format, new Object[] { arg0, arg1 });
	}

	/**
	 * Gets a string from the resource bundle and formats it with arguments.
	 */
	public static String getFormattedString(String key, Object[] args) {
		return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
	}

	/**
	 * Gives back the localized String corresponding to the given key.
	 * When there is no localized string available, the default string is
	 * returned. If that is missing too, the key sorrounded by exclamation
	 * marks is returned to hint the job of adding appropriate resource strings.
	 * 
	 * @param key  the key to the localized string
	 * @return  the possibly localized string or the annotated key
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}

// EOF
