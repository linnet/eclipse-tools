// $Header: /cvsroot/eclipse-tools/net.sourceforge.eclipsetools.quickmarks/src/net/sourceforge/eclipsetools/quickmarks/MarkPreferencePage.java,v 1.1 2004/07/16 00:59:08 deerwood Exp $

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

import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Georg Rehfeld, georg.rehfeld@gmx.de
 */
public class MarkPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage
{
	/** Name of preference to store the scope choice. */
	public static final String P_CHOICE_SCOPE = "choiceScopePreference"; //$NON-NLS-1$
	/** Name of preference to store the handling for multiple Quickmarks. */
	public static final String P_CHOICE_MULTIPLE = "choiceMultiplePreference"; //$NON-NLS-1$
	/** Name of preference to store the selection choice. */
	public static final String P_CHOICE_SELECTION = "choiceSelectionPreference"; //$NON-NLS-1$
	/** Name of preference to store the delete handling choice. */
	public static final String P_CHOICE_DELETE = "choiceDeletePreference"; //$NON-NLS-1$
	/** Name of preference to store the name pattern. */
	public static final String P_STRING_MARKERNAME = "stringMarkerNamePreference"; //$NON-NLS-1$

	/** Indicates that quickmark scope is workspace. */
	public static final String SCOPE_WORKSPACE = "workspace"; //$NON-NLS-1$
	/** Indicates that quickmark scope is project. */
	public static final String SCOPE_PROJECT = "project"; //$NON-NLS-1$
	/** Indicates that quickmark scope is current folder. */
	public static final String SCOPE_FOLDER = "folder"; //$NON-NLS-1$
	/** Indicates that quickmark scope is current document. */
	public static final String SCOPE_DOCUMENT = "document"; //$NON-NLS-1$

	/** Indicates that duplicate quickmarks should all be removed when setting
	 * a new one. */
	public static final String MULTIPLE_ADDNEVER = "addnever"; //$NON-NLS-1$
	/** Indicates that another duplicate should be added to already duplicate
	 * quickmarks when setting another one. */
	public static final String MULTIPLE_ADDMULTIPLE = "addmultiple"; //$NON-NLS-1$
	/** Indicates that the user should be asked how to handle duplicate
	 * quickmarks when setting a new one. */
	public static final String MULTIPLE_ADDASK = "addask"; //$NON-NLS-1$
	/** Indicates that duplicate quickmarks should be used all the time. */
	public static final String MULTIPLE_ADDALWAYS = "addalways"; //$NON-NLS-1$

	/** Indicates that the complete selection should be remembered in the quickmark. */
	public static final String SELECTION_KEEPALL = "keepall"; //$NON-NLS-1$
	/** Indicates that only start of selection should be remembered in the quickmark. */
	public static final String SELECTION_KEEPSTART = "keepstart"; //$NON-NLS-1$
	/** Indicates that start of selection line should be remembered in the quickmark. */
	public static final String SELECTION_LINESTART = "startline"; //$NON-NLS-1$
	/** Indicates that the first line of selection should be remembered in the quickmark. */
	public static final String SELECTION_WHOLELINE = "wholeline"; //$NON-NLS-1$

	/** Indicates that a delete is requested when the current selection
	 * completely matches the remebered one. */
	public static final String DELETE_MATCH = "match"; //$NON-NLS-1$
	/** Indicates that a delete is requested when the current selection starts
	 * at the same line as the remebered one. */
	public static final String DELETE_LINE = "line"; //$NON-NLS-1$

	/** The pattern editor, used to mediate changes to the sample editor. */
	private PatternFieldEditor patternEditor = null;
	/** The sample editor (readonly), used to mediate changes from the pattern editor. */
	private StringFieldEditor sampleEditor = null;

	/**
	 * A specialized StringFieldEditor able to continuously check it's input
	 * value for beeing a valid MessageFormat pattern. Instances also reformat
	 * the input pattern on every change to it with some sample parameters and
	 * signal changes more often than the superclass. The latter neccessary to
	 * have other editors show the formatted pattern in all situations.
	 */
	private class PatternFieldEditor extends StringFieldEditor {
		/** Storage for the sample-formatted pattern. */
		private String samplePattern = ""; //$NON-NLS-1$

		/**
		 * Creates a pattern field editor of unlimited width.
		 * Use the method <code>setTextLimit</code> to limit the text.
		 * 
		 * @param name the name of the preference this field editor works on
		 * @param labelText the label text of the field editor
		 * @param parent the parent of the field editor's control
		 */
		public PatternFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * Deliveres the pattern formatted with sample values.
		 * 
		 * @return  the formatted pattern
		 */
		public String getSamplePattern() {
			return samplePattern;
		}

		/**
		 * Checks the input field of this editor for validness as a
		 * MessageFormat pattern, by trying to format it with sample values
		 * of the expected type (currently hardcoded in this method).
		 * When the check succeeds the formatted pattern may be retrieved with
		 * <code>getSamplePattern()</code> and the return is true. Else
		 * the error message and the sample pattern is set to a string
		 * indicating the problem and false is returned.
		 * <p>
		 * This method also always signals a value changed, so that property
		 * change listeners are informed immediately.
		 * 
		 * @return  true, when the input is valid, false otherwise
		 */
		protected boolean doCheckState() {
			try {
				clearErrorMessage();
				samplePattern = MessageFormat.format(getStringValue(),
					new Object[] {
						new Integer(0),
						new Integer(42),
						new Integer(333),
						new Integer(13),
						"readme.txt", //$NON-NLS-1$
						"doc", //$NON-NLS-1$
						"src/doc", //$NON-NLS-1$
						"org.eclipse.example", //$NON-NLS-1$
						new Date(System.currentTimeMillis()),
						"before ", //$NON-NLS-1$
						"the selection", //$NON-NLS-1$
						" and after" //$NON-NLS-1$
					}
				);
			}
			catch (IllegalArgumentException e) {
				samplePattern = Messages.getString(
					"MarkPreferencePage.msg.InvalidPattern") + //$NON-NLS-1$
					e.getLocalizedMessage();
				setErrorMessage(samplePattern);
				fireValueChanged(VALUE, getStringValue(), getStringValue());
				return false;
			}
			fireValueChanged(VALUE, getStringValue(), getStringValue());
			return true;
		}
	} // class PatternFieldEditor

	/**
	 * Constructs a new property page for Quickmarks.
	 */
	public MarkPreferencePage() {
		super(GRID);
		setPreferenceStore(QuickmarksPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.getString("MarkPreferencePage.Description")); //$NON-NLS-1$
		initializeDefaults();
	}
	/**
	 * Sets the default values of the preferences.
	 */
	private void initializeDefaults() {
		// done in plugin class
		/*
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(P_CHOICE_SCOPE, SCOPE_WORKSPACE);
		store.setDefault(P_CHOICE_MULTIPLE, MULTIPLE_ADDMULTIPLE);
		store.setDefault(P_CHOICE_SELECTION, SELECTION_KEEPALL);
		store.setDefault(P_CHOICE_DELETE, DELETE_MATCH);
		store.setDefault(P_STRING_MARKERNAME, "Quickmark ");
		*/
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		RadioGroupFieldEditor rgfEditor = new RadioGroupFieldEditor(
			P_CHOICE_SCOPE,
			Messages.getString("MarkPreferencePage.title.Scope"), //$NON-NLS-1$
			1,
			new String[][] { 
				{ Messages.getString("MarkPreferencePage.label.scope.workspace"), SCOPE_WORKSPACE }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.scope.project"), SCOPE_PROJECT }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.scope.folder"), SCOPE_FOLDER }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.scope.document"), SCOPE_DOCUMENT }, //$NON-NLS-1$
			},
			parent,
			true);
		addField(rgfEditor);
		Control control = rgfEditor.getRadioBoxControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_scope"); //$NON-NLS-1$

		parent = getFieldEditorParent();
		rgfEditor = new RadioGroupFieldEditor(
			P_CHOICE_MULTIPLE,
			Messages.getString("MarkPreferencePage.title.Multiple"), //$NON-NLS-1$
			1,
			new String[][] {
				{ Messages.getString("MarkPreferencePage.label.multiple.addnever"), MULTIPLE_ADDNEVER }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.multiple.addmultiple"), MULTIPLE_ADDMULTIPLE }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.multiple.addask"), MULTIPLE_ADDASK }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.multiple.addalways"), MULTIPLE_ADDALWAYS }, //$NON-NLS-1$
			}, 
			parent,
			true);
		addField(rgfEditor);
		control = rgfEditor.getRadioBoxControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_multiple"); //$NON-NLS-1$

		parent = getFieldEditorParent();
		rgfEditor = new RadioGroupFieldEditor(
			P_CHOICE_SELECTION,
			Messages.getString("MarkPreferencePage.title.Selection"), //$NON-NLS-1$
			1,
			new String[][] { 
				{ Messages.getString("MarkPreferencePage.label.selection.keepall"), SELECTION_KEEPALL }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.selection.keepstart"), SELECTION_KEEPSTART }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.selection.startline"), SELECTION_LINESTART }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.selection.wholeline"), SELECTION_WHOLELINE },  //$NON-NLS-1$
			}, 
			parent,
			true);
		addField(rgfEditor);
		control = rgfEditor.getRadioBoxControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_selection"); //$NON-NLS-1$

		parent = getFieldEditorParent();
		rgfEditor = new RadioGroupFieldEditor(
			P_CHOICE_DELETE,
			Messages.getString("MarkPreferencePage.title.Delete"), //$NON-NLS-1$
			1,
			new String[][] { 
				{ Messages.getString("MarkPreferencePage.label.delete.match"), DELETE_MATCH }, //$NON-NLS-1$
				{ Messages.getString("MarkPreferencePage.label.delete.line"), DELETE_LINE }, //$NON-NLS-1$
			}, 
			parent,
			true);
		addField(rgfEditor);
		control = rgfEditor.getRadioBoxControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_delete"); //$NON-NLS-1$

		parent = getFieldEditorParent();
		this.sampleEditor = new StringFieldEditor(
			"unused",  //$NON-NLS-1$
			Messages.getString("MarkPreferencePage.label.formattetName"), //$NON-NLS-1$
			parent);
		addField(this.sampleEditor);
		control = this.sampleEditor.getTextControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_basename"); //$NON-NLS-1$
		this.sampleEditor.getTextControl(parent).setEditable(false);

		parent = getFieldEditorParent();
		this.patternEditor = new PatternFieldEditor(
			P_STRING_MARKERNAME, 
			Messages.getString("MarkPreferencePage.label.pattern"), //$NON-NLS-1$
			parent);
		addField(this.patternEditor);
		control = this.patternEditor.getTextControl(parent);
		WorkbenchHelp.setHelp(control, "net.sourceforge.eclipsetools.quickmarks.ctx_pref_pattern"); //$NON-NLS-1$
		control.setToolTipText(Messages.getString("MarkPreferencePage.tooltip.pattern")); //$NON-NLS-1$
	}

	/**
	 * Overridden to mediate between the pattern editor and it's accompanied
	 * sample editor.
	 */
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource().equals(patternEditor)) {
			sampleEditor.setStringValue(patternEditor.getSamplePattern());
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
}

// EOF
