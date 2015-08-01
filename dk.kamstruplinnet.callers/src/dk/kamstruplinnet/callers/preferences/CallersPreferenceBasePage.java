package dk.kamstruplinnet.callers.preferences;

import dk.kamstruplinnet.callers.CallersConstants;
import dk.kamstruplinnet.callers.CallersPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * @see PreferencePage
 */
public class CallersPreferenceBasePage extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {
    public CallersPreferenceBasePage() {
        super(FieldEditorPreferencePage.GRID);

        // Set the preference store for the preference page.
        IPreferenceStore store = CallersPlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    /**
     * @see PreferencePage#init
     */
    public void init(IWorkbench workbench) {
    }

    /**
     * Set the default preferences for this page.
     */
    public static void initDefaults(IPreferenceStore store) {
        CallersPlugin.getDefault().initializeDefaultBasePreferences(store);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        IntegerFieldEditor maxCallDepth = new IntegerFieldEditor(CallersConstants.PREF_MAX_CALL_DEPTH,
                "&Max call depth", getFieldEditorParent());
        maxCallDepth.setValidRange(1, 99);
        addField(maxCallDepth);

        RadioGroupFieldEditor callDetailOrientation = new RadioGroupFieldEditor(CallersConstants.PREF_DETAIL_SASH_ORIENTATION,
                "&Detail orientation", 1,
                new String[][] {
                    { "&Vertical", CallersConstants.DETAIL_ORIENTATION_VERTICAL },
                    { "&Horizontal", CallersConstants.DETAIL_ORIENTATION_HORIZONTAL }
                }, getFieldEditorParent());
        addField(callDetailOrientation);

        RadioGroupFieldEditor javaLabelFormat = new RadioGroupFieldEditor(CallersConstants.PREF_JAVA_LABEL_FORMAT,
                "&Java label format", 1,
                new String[][] {
                    { "D&efault", CallersConstants.JAVA_FORMAT_DEFAULT },
                    { "&Long (parameters, type)", CallersConstants.JAVA_FORMAT_LONG },
                    { "&Short (only method name)", CallersConstants.JAVA_FORMAT_SHORT }
                }, getFieldEditorParent());
        addField(javaLabelFormat);

        BooleanFieldEditor useImplementorsForCallerSearch = new BooleanFieldEditor(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH,
                "Search for &callers using the Implementors plugin", getFieldEditorParent());
        useImplementorsForCallerSearch.setEnabled(CallersPlugin.getDefault().isImplementorsPluginAvailable(), getFieldEditorParent());
        addField(useImplementorsForCallerSearch);

        BooleanFieldEditor useImplementorsForCalleeSearch = new BooleanFieldEditor(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH,
                "Sea&rch for callees using the Implementors plugin", getFieldEditorParent());
        useImplementorsForCalleeSearch.setEnabled(CallersPlugin.getDefault().isImplementorsPluginAvailable(), getFieldEditorParent());
        addField(useImplementorsForCalleeSearch);

        // This should be reenabled when the openInEditor(Object, boolean) method is made API.
        //        BooleanFieldEditor activateEditorOnSelect = new BooleanFieldEditor(CallersConstants.PREF_ACTIVATE_EDITOR_ON_SELECT,
        //                "&Activate editor on select", getFieldEditorParent());
        //        addField(activateEditorOnSelect);
    }
}
