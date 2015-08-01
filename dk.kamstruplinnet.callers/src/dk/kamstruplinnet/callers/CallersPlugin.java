package dk.kamstruplinnet.callers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import dk.kamstruplinnet.callers.views.CallersView;
import dk.kamstruplinnet.implementors.core.Implementors;


/**
 * The main plugin class to be used in the desktop.
 */
public class CallersPlugin extends AbstractUIPlugin implements IPropertyChangeListener {
    private Implementors mImplementorsPlugin;
    private IJavaSearchScope mSearchScope;
    private static final String[] IGNORE_FILTERS = { "java" };
    //The shared instance.
    private static CallersPlugin plugin;

    /**
     * Returns the shared instance.
     */
    public static CallersPlugin getDefault() {
        return plugin;
    }

    /**
     * Method getPluginId.
     */
    public static String getPluginId() {
        return CallersConstants.PLUGIN_ID;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static void logDebug(String message) {
        if (getDefault().isDebugging()) {
            getDefault().getLog().log(new Status(IStatus.INFO, getPluginId(), IStatus.OK,
                    message, null));
        }
    }

    public static void logError(String message, Throwable t) {
        getDefault().getLog().log(new Status(IStatus.ERROR, getPluginId(),
                IStatus.ERROR, message, t));
    }

    /**
     * Parses the comma separated string into an array of strings
     *
     * @return list
     */
    public static String[] parseList(String listString) {
        List list = new ArrayList(10);
        StringTokenizer tokenizer = new StringTokenizer(listString, ","); //$NON-NLS-1$

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            list.add(token);
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    private int mMaxCallDepth = -1;

    /**
     * The constructor.
     */
    public CallersPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
        plugin = this;
    }

    /**
     * @param string
     * @return int
     */
    private int convertJavaLabelSettingsString(String setting) {
        int result = JavaElementLabelProvider.SHOW_PARAMETERS |
            JavaElementLabelProvider.SHOW_POST_QUALIFIED;

        if (setting != null) {
            try {
                result = Integer.parseInt(setting);
            } catch (NumberFormatException nfe) {
            }
        }

        return result;
    }

    /**
     * @param string
     * @return int
     */
    private int convertOrientationString(String orientationString) {
        if (CallersConstants.DETAIL_ORIENTATION_HORIZONTAL.equals(orientationString)) {
            return SWT.HORIZONTAL;
        } else {
            return SWT.VERTICAL;
        }
    }

    public int getCallDetailOrientation() {
        IPreferenceStore settings = getDefault().getPreferenceStore();

        try {
            return convertOrientationString(settings.getString(
                    CallersConstants.PREF_DETAIL_SASH_ORIENTATION));
        } catch (Exception e) {
            logError("Error getting sash orientation", e);
        }

        return convertOrientationString(settings.getDefaultString(
                CallersConstants.PREF_DETAIL_SASH_ORIENTATION));
    }

    public boolean getActivateEditorOnSelect() {
        IPreferenceStore settings = getDefault().getPreferenceStore();

        try {
            return settings.getBoolean(CallersConstants.PREF_ACTIVATE_EDITOR_ON_SELECT);
        } catch (Exception e) {
            logError("Error getting sash orientation", e);
        }

        return settings.getDefaultBoolean(CallersConstants.PREF_ACTIVATE_EDITOR_ON_SELECT);
    }
    
    /**
     * Returns filters for packages which should not be included in the search
     * results.
     * @return String[]
     */
    public String[] getIgnoreFilters() {
        try {
            IPreferenceStore settings = getDefault().getPreferenceStore();

            if (settings.getBoolean(CallersConstants.PREF_USE_FILTERS)) {
                String[] strings = parseList(settings.getString(
                            CallersConstants.PREF_ACTIVE_FILTERS_LIST));

                return strings;
            } else {
                return null;
            }
        } catch (Exception e) {
            logError("Error getting ignore filters", e);
        }

        return IGNORE_FILTERS;
    }

    /**
     * Gets the settings for the format of the Java label provider.
     */
    public int getJavaLabelSettings() {
        IPreferenceStore settings = getDefault().getPreferenceStore();

        try {
            return convertJavaLabelSettingsString(settings.getString(
                    CallersConstants.PREF_JAVA_LABEL_FORMAT));
        } catch (Exception e) {
            logError("Error getting java label settings", e);
        }

        return convertJavaLabelSettingsString(settings.getDefaultString(
                CallersConstants.PREF_JAVA_LABEL_FORMAT));
    }

    /**
     * Returns the maximum tree level allowed
     * @return int
     */
    public int getMaxCallDepth() {
        if (mMaxCallDepth == -1) {
            IPreferenceStore settings = getDefault().getPreferenceStore();

            try {
                mMaxCallDepth = settings.getInt(
                        CallersConstants.PREF_MAX_CALL_DEPTH);
            } catch (Exception e) {
                logError("Error getting sash orientation", e);
            }
        }
        return mMaxCallDepth;
    }

    public void initializeDefaultBasePreferences(IPreferenceStore store) {
        store.setDefault(CallersConstants.PREF_MAX_CALL_DEPTH, 10);
        store.setDefault(CallersConstants.PREF_DETAIL_SASH_ORIENTATION,
            CallersConstants.DETAIL_ORIENTATION_HORIZONTAL);
        store.setDefault(CallersConstants.PREF_JAVA_LABEL_FORMAT,
            CallersConstants.JAVA_FORMAT_LONG);
        store.setDefault(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH, false);
        store.setDefault(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH, false);
        store.setDefault(CallersConstants.PREF_ACTIVATE_EDITOR_ON_SELECT, true);
    }

    public void initializeDefaultFilterPreferences(IPreferenceStore store) {
        store.setDefault(CallersConstants.PREF_ACTIVE_FILTERS_LIST, "javax.*,java.*");
        store.setDefault(CallersConstants.PREF_INACTIVE_FILTERS_LIST,
            "com.ibm.*,com.sun.*,org.omg.*,sun.*,sunw.*"); //$NON-NLS-1$
        store.setDefault(CallersConstants.PREF_USE_FILTERS, true);
    }

    public void initializeDefaultPreferences(IPreferenceStore store) {
        initializeDefaultBasePreferences(store);
        initializeDefaultFilterPreferences(store);
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(CallersConstants.PREF_MAX_CALL_DEPTH)) {
            try {
                mMaxCallDepth = ((Integer)event.getNewValue()).intValue();
            } catch (Exception e) {
                logError("Error setting new value of max call depth", e);
            }
        }
    }

    public void setSearchScope(IJavaSearchScope searchScope) {
        this.mSearchScope = searchScope; 
    }
    
    public IJavaSearchScope getSearchScope() {
        if (mSearchScope != null) {
            return mSearchScope;
        }
        logDebug("No search scope was set");
        return SearchEngine.createWorkspaceScope();
    }
    
    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#shutdown()
     */
    public void shutdown() throws CoreException {
        getPreferenceStore().removePropertyChangeListener(this);
        super.shutdown();
        logDebug("Plugin shut down");
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#startup()
     */
    public void startup() throws CoreException {
        super.startup();
        logDebug("Plugin started");
        getPreferenceStore().addPropertyChangeListener(this);
    }

    /**
     * @return
     */
    public boolean isSearchForCallersUsingImplementorsEnabled() {
        if (isImplementorsPluginAvailable()) {
            IPreferenceStore settings = getDefault().getPreferenceStore();

            try {
                return settings.getBoolean(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH);
            } catch (Exception e) {
                logError("Error establishing whether searching for callers with Implementors is enabled", e);
            }

            return settings.getDefaultBoolean(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLER_SEARCH);
        }
        return false;
    }

    /**
     * @return
     */
    public boolean isSearchForCalleesUsingImplementorsEnabled() {
        if (isImplementorsPluginAvailable()) {
            IPreferenceStore settings = getDefault().getPreferenceStore();

            try {
                return settings.getBoolean(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH);
            } catch (Exception e) {
                logError("Error establishing whether searching for callees with Implementors is enabled", e);
            }

            return settings.getDefaultBoolean(CallersConstants.PREF_USE_IMPLEMENTORS_FOR_CALLEE_SEARCH);
        }
        return false;
    }

    /**
     * @param method
     * @return
     */
    public Collection getInterfaceMethods(IMethod method) {
        if (isSearchForCallersUsingImplementorsEnabled()) {
            IJavaElement[] result = getImplementorsPlugin().searchForInterfacesAndAbstractTypes(new IJavaElement[] { method }, new NullProgressMonitor());
            if (result != null && result.length > 0) {
                return Arrays.asList(result);
            }
        }
        return new ArrayList(0);
    }

    /**
     * @param method
     * @return
     */
    public Collection getImplementingMethods(IMethod method) {
        if (isSearchForCalleesUsingImplementorsEnabled()) {
            IJavaElement[] result = getImplementorsPlugin().searchForImplementors(new IJavaElement[] { method }, new NullProgressMonitor());
            if (result != null && result.length > 0) {
                return Arrays.asList(result);
            }
        }
        return new ArrayList(0);
    }


    private Implementors getImplementorsPlugin() {
        if (mImplementorsPlugin == null) {
            IPluginRegistry registry = Platform.getPluginRegistry();
            IPluginDescriptor descriptor = registry.getPluginDescriptor("dk.kamstruplinnet.implementors.core");
            if (descriptor != null) {
                try {
                    mImplementorsPlugin = (Implementors) descriptor.getPlugin();
                } catch (CoreException e) {
                    logError("Error getting the Implementors plugin (", e);
                }
            }
        }
                        
        return mImplementorsPlugin;
    }
    
    /**
     * @return
     */
    public IProgressMonitor getProgressMonitor() {
        IProgressMonitor progressMonitor = getStatusLineManager().getProgressMonitor();
        return progressMonitor;
    }

    private IStatusLineManager getStatusLineManager() {
        IStatusLineManager statusLineManager = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(CallersView.CALLERS_VIEW_ID).getViewSite().getActionBars().getStatusLineManager();
        statusLineManager.setCancelEnabled(true);
        return statusLineManager;
    }

    /**
     * @return
     */
    public boolean isImplementorsPluginAvailable() {
        return getImplementorsPlugin() != null;
    }
    
}
